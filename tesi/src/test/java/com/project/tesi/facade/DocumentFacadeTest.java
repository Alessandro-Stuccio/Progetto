package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.facade.impl.DocumentFacadeImpl;
import com.project.tesi.mapper.DocumentMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentFacadeTest {

    @Mock private DocumentService documentService;
    @Mock private ActivityFeedFacade activityFeedFacade;
    @Mock private UserService userService;
    @Mock private DocumentMapper documentMapper;

    @InjectMocks private DocumentFacadeImpl documentFacade;

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

    @Test
    @DisplayName("PT carica WORKOUT_PLAN — successo")
    void ptCanUploadWorkoutPlan() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Document doc = buildDoc("scheda.pdf", DocumentType.WORKOUT_PLAN);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getUserById(2L)).thenReturn(pt);
        when(documentService.uploadDocument(file, 1L, 2L, "WORKOUT_PLAN")).thenReturn(doc);

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

        verify(documentService, never()).uploadDocument(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Nutrizionista carica DIET_PLAN — successo")
    void nutritionistCanUploadDietPlan() {
        User nutri = buildUser(3L, Role.NUTRITIONIST);
        Document doc = buildDoc("dieta.pdf", DocumentType.DIET_PLAN);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getUserById(3L)).thenReturn(nutri);
        when(documentService.uploadDocument(file, 1L, 3L, "DIET_PLAN")).thenReturn(doc);

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

        verify(documentService, never()).uploadDocument(any(), any(), any(), any());
    }

    @Test
    @DisplayName("CLIENT carica INSURANCE_POLICE — successo (nessuna restrizione di ruolo)")
    void clientCanUploadAnyType() {
        User client = buildUser(1L, Role.CLIENT);
        Document doc = buildDoc("polizza.pdf", DocumentType.INSURANCE_POLICE);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.uploadDocument(file, 1L, 1L, "INSURANCE_POLICE")).thenReturn(doc);

        DocumentUploadResponse result = documentFacade.uploadDocumentWithValidation(file, 1L, 1L, "INSURANCE_POLICE");

        assertThat(result.type()).isEqualTo("INSURANCE_POLICE");
    }

    @Test
    @DisplayName("upload riuscito — activityFeedFacade viene notificato")
    void activityFeedIsLoggedOnSuccess() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Document doc = buildDoc("scheda.pdf", DocumentType.WORKOUT_PLAN);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getUserById(2L)).thenReturn(pt);
        when(documentService.uploadDocument(file, 1L, 2L, "WORKOUT_PLAN")).thenReturn(doc);

        documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN");

        verify(activityFeedFacade).logDocumentUploaded(1L, 2L, "WORKOUT_PLAN");
    }
}
