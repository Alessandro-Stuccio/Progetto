package com.project.tesi.service.impl;

import com.project.tesi.dto.response.UpdatedNotesResponse;
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
    private User client, pt;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        pt = User.builder().email("pt@test.com").password("testpass").id(2L).firstName("Luca").lastName("Bianchi").role(Role.PERSONAL_TRAINER).build();
        client = User.builder().email("client@test.com").password("testpass").id(1L).firstName("Mario").lastName("Rossi").role(Role.CLIENT).build();
    }

    // ─── uploadDocument ───────────────────────────────────────────────────────

    @Test @DisplayName("uploadDocument — salva file e restituisce Document")
    void uploadDocument_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("scheda.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        Document saved = Document.builder().id(1L).fileName("scheda.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.save(any())).thenReturn(saved);

        Document result = documentService.uploadDocument(file, 1L, 2L, "WORKOUT_PLAN");
        assertThat(result.getFileName()).isEqualTo("scheda.pdf");
        assertThat(result.getType()).isEqualTo(DocumentType.WORKOUT_PLAN);
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

    @Test @DisplayName("uploadDocument — uploader non trovato lancia ResourceNotFoundException")
    void uploadDocument_uploaderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.uploadDocument(
                mock(MultipartFile.class), 1L, 999L, "WORKOUT_PLAN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── downloadDocument ─────────────────────────────────────────────────────

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

    // ─── getDocumentById ──────────────────────────────────────────────────────

    @Test @DisplayName("getDocumentById — trovato")
    void getDocumentById_success() {
        Document doc = Document.builder().id(1L).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        assertThat(documentService.getDocumentById(1L)).isEqualTo(doc);
    }

    // ─── getUserDocuments ─────────────────────────────────────────────────────

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

    // ─── deleteDocument ───────────────────────────────────────────────────────

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

    @Test @DisplayName("deleteDocument — documento non trovato lancia eccezione")
    void deleteDocument_notFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> documentService.deleteDocument(999L)).isInstanceOf(DocumentNotFoundException.class);
    }

    // ─── updateNotes ──────────────────────────────────────────────────────────

    @Test @DisplayName("updateNotes — aggiorna le note")
    void updateNotes() {
        Document doc = Document.builder().id(1L).fileName("f.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentRepository.save(doc)).thenReturn(doc);

        documentService.updateNotes(1L, "Nuove note");
        assertThat(doc.getNotes()).isEqualTo("Nuove note");
    }

    // ─── saveDocument ─────────────────────────────────────────────────────────

    @Test @DisplayName("saveDocument — salva e restituisce documento")
    void saveDocument() {
        Document doc = Document.builder().id(1L).build();
        when(documentRepository.save(doc)).thenReturn(doc);
        assertThat(documentService.saveDocument(doc)).isEqualTo(doc);
    }
}
