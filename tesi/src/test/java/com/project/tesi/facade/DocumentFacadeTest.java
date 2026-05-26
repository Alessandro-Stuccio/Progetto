package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.facade.impl.DocumentFacadeImpl;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.FileStorageService;
import com.project.tesi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentFacadeTest {

    @Mock private DocumentService documentService;
    @Mock private FileStorageService fileStorageService;
    @Mock private ActivityFeedService activityFeedService;
    @Mock private UserService userService;
    @Mock private DocumentMapper documentMapper;

    private DocumentFacadeImpl documentFacade;

    @BeforeEach
    void setUp() {
        documentFacade = new DocumentFacadeImpl(
                documentService, fileStorageService, activityFeedService, userService, documentMapper);
    }

    private User buildUser(Long id, Role role) {
        return User.builder()
                .id(id)
                .email(role.name().toLowerCase() + "@test.com")
                .password("password123")
                .firstName("Test").lastName("User")
                .role(role)
                .build();
    }

    private Document buildDoc(String fileName, DocumentType type) {
        return Document.builder()
                .id(1L)
                .fileName(fileName)
                .type(type)
                .uploadDate(LocalDateTime.now())
                .build();
    }

    private MultipartFile mockFile(String name) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(name);
        when(file.getContentType()).thenReturn("application/pdf");
        return file;
    }

    @Test
    @DisplayName("PT carica WORKOUT_PLAN — successo")
    void ptCanUploadWorkoutPlan() throws IOException {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Document doc = buildDoc("scheda.pdf", DocumentType.WORKOUT_PLAN);
        MultipartFile file = mockFile("scheda.pdf");

        when(userService.getUserById(2L)).thenReturn(pt);
        when(fileStorageService.store(file)).thenReturn("/uploads/scheda.pdf");
        when(documentService.uploadDocument(anyString(), eq("scheda.pdf"), eq("application/pdf"),
                eq("WORKOUT_PLAN"), eq(1L), eq(2L))).thenReturn(doc);

        DocumentUploadResponse result = documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN");

        assertThat(result.fileName()).isEqualTo("scheda.pdf");
        assertThat(result.type()).isEqualTo("WORKOUT_PLAN");
    }

    @Test
    @DisplayName("PT carica DIET_PLAN — lancia InvalidFileException")
    void ptCannotUploadDietPlan() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        when(userService.getUserById(2L)).thenReturn(pt);

        assertThatThrownBy(() ->
                documentFacade.uploadDocumentWithValidation(mock(MultipartFile.class), 1L, 2L, "DIET_PLAN"))
                .isInstanceOf(InvalidFileException.class);

        verify(documentService, never()).uploadDocument(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Nutrizionista carica DIET_PLAN — successo")
    void nutritionistCanUploadDietPlan() throws IOException {
        User nutri = buildUser(3L, Role.NUTRITIONIST);
        Document doc = buildDoc("dieta.pdf", DocumentType.DIET_PLAN);
        MultipartFile file = mockFile("dieta.pdf");

        when(userService.getUserById(3L)).thenReturn(nutri);
        when(fileStorageService.store(file)).thenReturn("/uploads/dieta.pdf");
        when(documentService.uploadDocument(anyString(), eq("dieta.pdf"), eq("application/pdf"),
                eq("DIET_PLAN"), eq(1L), eq(3L))).thenReturn(doc);

        DocumentUploadResponse result = documentFacade.uploadDocumentWithValidation(file, 1L, 3L, "DIET_PLAN");

        assertThat(result.fileName()).isEqualTo("dieta.pdf");
        assertThat(result.type()).isEqualTo("DIET_PLAN");
    }

    @Test
    @DisplayName("Nutrizionista carica WORKOUT_PLAN — lancia InvalidFileException")
    void nutritionistCannotUploadWorkoutPlan() {
        User nutri = buildUser(3L, Role.NUTRITIONIST);
        when(userService.getUserById(3L)).thenReturn(nutri);

        assertThatThrownBy(() ->
                documentFacade.uploadDocumentWithValidation(mock(MultipartFile.class), 1L, 3L, "WORKOUT_PLAN"))
                .isInstanceOf(InvalidFileException.class);

        verify(documentService, never()).uploadDocument(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CLIENT carica INSURANCE_POLICE — successo (nessuna restrizione di ruolo)")
    void clientCanUploadAnyType() throws IOException {
        User client = buildUser(1L, Role.CLIENT);
        Document doc = buildDoc("polizza.pdf", DocumentType.INSURANCE_POLICE);
        MultipartFile file = mockFile("polizza.pdf");

        when(userService.getUserById(1L)).thenReturn(client);
        when(fileStorageService.store(file)).thenReturn("/uploads/polizza.pdf");
        when(documentService.uploadDocument(anyString(), eq("polizza.pdf"), eq("application/pdf"),
                eq("INSURANCE_POLICE"), eq(1L), eq(1L))).thenReturn(doc);

        DocumentUploadResponse result = documentFacade.uploadDocumentWithValidation(file, 1L, 1L, "INSURANCE_POLICE");

        assertThat(result.type()).isEqualTo("INSURANCE_POLICE");
    }

    @Test
    @DisplayName("upload riuscito — activityFeedService viene notificato")
    void activityFeedIsLoggedOnSuccess() throws IOException {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Document doc = buildDoc("scheda.pdf", DocumentType.WORKOUT_PLAN);
        MultipartFile file = mockFile("scheda.pdf");

        when(userService.getUserById(2L)).thenReturn(pt);
        when(fileStorageService.store(file)).thenReturn("/uploads/scheda.pdf");
        when(documentService.uploadDocument(anyString(), eq("scheda.pdf"), eq("application/pdf"),
                eq("WORKOUT_PLAN"), eq(1L), eq(2L))).thenReturn(doc);

        documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN");

        verify(activityFeedService).logDocumentUploaded(1L, 2L, "WORKOUT_PLAN");
    }
}
