package com.project.tesi.service.impl;

import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.document.DocumentNotFoundException;
import com.project.tesi.exception.document.DocumentStorageException;
import com.project.tesi.exception.document.InvalidFileException;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private DocumentServiceImpl documentService;

    @TempDir Path tempDir;
    private User client, pt, nutri;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        pt = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(2L).firstName("Luca").role(Role.PERSONAL_TRAINER).build();
        nutri = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(3L).firstName("Sara").role(Role.NUTRITIONIST).build();
        client = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(1L).firstName("Mario").role(Role.CLIENT).build();
    }

    @Test @DisplayName("uploadDocumentWithValidation — PT carica WORKOUT_PLAN OK")
    void uploadDocument_ptWorkoutPlan() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("scheda.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));

        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        Document saved = Document.builder().id(1L).fileName("scheda.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.save(any())).thenReturn(saved);

        DocumentUploadResponse result = documentService.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN");
        assertThat(result.fileName()).isEqualTo("scheda.pdf");
    }

    @Test @DisplayName("uploadDocumentWithValidation — PT carica DIET_PLAN → InvalidFileException")
    void uploadDocument_ptWrongType() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        assertThatThrownBy(() -> documentService.uploadDocumentWithValidation(
                mock(MultipartFile.class), 1L, 2L, "DIET_PLAN")).isInstanceOf(InvalidFileException.class);
    }

    @Test @DisplayName("uploadDocumentWithValidation — Nutrizionista carica WORKOUT_PLAN → InvalidFileException")
    void uploadDocument_nutriWrongType() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(3L)).thenReturn(Optional.of(nutri));
        assertThatThrownBy(() -> documentService.uploadDocumentWithValidation(
                mock(MultipartFile.class), 1L, 3L, "WORKOUT_PLAN")).isInstanceOf(InvalidFileException.class);
    }

    @Test @DisplayName("uploadDocument — nome file null → InvalidFileException")
    void uploadDocument_nullFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));

        assertThatThrownBy(() -> documentService.uploadDocument(file, 1L, 2L, "WORKOUT_PLAN"))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test @DisplayName("uploadDocument — nome file senza estensione → InvalidFileException")
    void uploadDocument_noExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("filenoext");
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));

        assertThatThrownBy(() -> documentService.uploadDocument(file, 1L, 2L, "WORKOUT_PLAN"))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test @DisplayName("downloadDocument — documento trovato")
    void downloadDocument_success() throws IOException {
        Path testFile = tempDir.resolve("test.pdf");
        Files.write(testFile, new byte[]{1, 2, 3});
        Document doc = Document.builder().id(1L).filePath(testFile.toString()).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        byte[] data = documentService.downloadDocument(1L);
        assertThat(data).hasSize(3);
    }

    @Test @DisplayName("downloadDocument — documento non trovato")
    void downloadDocument_notFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> documentService.downloadDocument(999L)).isInstanceOf(DocumentNotFoundException.class);
    }

    @Test @DisplayName("downloadDocument — file non leggibile → DocumentStorageException")
    void downloadDocument_ioError() {
        Document doc = Document.builder().id(1L).filePath("/nonexistent/path.pdf").build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        assertThatThrownBy(() -> documentService.downloadDocument(1L)).isInstanceOf(DocumentStorageException.class);
    }

    @Test @DisplayName("getDocumentById — trovato")
    void getDocumentById_success() {
        Document doc = Document.builder().id(1L).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        assertThat(documentService.getDocumentById(1L)).isEqualTo(doc);
    }

    @Test @DisplayName("getUserDocuments — restituisce documenti dell'utente")
    void getUserDocuments() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        Document doc = Document.builder().id(1L).fileName("test.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.findByOwnerOrderByUploadDateDesc(client)).thenReturn(List.of(doc));

        List<Document> result = documentService.getUserDocuments(1L);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getUserDocumentsByType — filtrato per tipo")
    void getUserDocumentsByType() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(client, DocumentType.WORKOUT_PLAN))
                .thenReturn(List.of());

        List<Document> result = documentService.getUserDocumentsByType(1L, "WORKOUT_PLAN");
        assertThat(result).isEmpty();
    }

    @Test @DisplayName("deleteDocument — elimina dal db e dal filesystem")
    void deleteDocument_success() throws IOException {
        Path testFile = tempDir.resolve("to-delete.pdf");
        Files.write(testFile, new byte[]{1});
        Document doc = Document.builder().id(1L).filePath(testFile.toString()).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L);

        verify(documentRepository).delete(doc);
        assertThat(Files.exists(testFile)).isFalse();
    }

    @Test @DisplayName("updateNotes — aggiorna le note")
    void updateNotes() {
        Document doc = Document.builder().id(1L).fileName("f.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentRepository.save(doc)).thenReturn(doc);

        documentService.updateNotes(1L, "Nuove note");
        assertThat(doc.getNotes()).isEqualTo("Nuove note");
    }

    // ══════════════ BRANCH AGGIUNTIVE ══════════════

    @Test @DisplayName("uploadDocumentWithValidation — Nutrizionista carica DIET_PLAN OK")
    void uploadDocument_nutriDietPlan() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("dieta.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        when(userRepository.findById(3L)).thenReturn(Optional.of(nutri));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        Document saved = Document.builder().id(1L).fileName("dieta.pdf").type(DocumentType.DIET_PLAN)
                .owner(client).uploadedBy(nutri).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.save(any())).thenReturn(saved);

        DocumentUploadResponse result = documentService.uploadDocumentWithValidation(file, 1L, 3L, "DIET_PLAN");
        assertThat(result.fileName()).isEqualTo("dieta.pdf");
    }

    @Test @DisplayName("uploadDocumentWithValidation — CLIENT può caricare qualsiasi tipo")
    void uploadDocument_clientAnyType() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("cert.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        Document saved = Document.builder().id(1L).fileName("cert.pdf").type(DocumentType.INSURANCE_POLICE)
                .owner(client).uploadedBy(client).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.save(any())).thenReturn(saved);

        DocumentUploadResponse result = documentService.uploadDocumentWithValidation(file, 1L, 1L, "INSURANCE_POLICE");
        assertThat(result.type()).isEqualTo("INSURANCE_POLICE");
    }

    @Test @DisplayName("saveDocument — salva e restituisce documento")
    void saveDocument() {
        Document doc = Document.builder().id(1L).build();
        when(documentRepository.save(doc)).thenReturn(doc);
        assertThat(documentService.saveDocument(doc)).isEqualTo(doc);
    }

    @Test @DisplayName("deleteDocument — documento non trovato lancia eccezione")
    void deleteDocument_notFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> documentService.deleteDocument(999L)).isInstanceOf(DocumentNotFoundException.class);
    }

    @Test @DisplayName("uploadDocumentWithValidation — uploader non trovato lancia eccezione")
    void uploadDocument_uploaderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> documentService.uploadDocumentWithValidation(
                mock(MultipartFile.class), 1L, 999L, "WORKOUT_PLAN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


