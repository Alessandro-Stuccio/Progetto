package com.project.tesi.service.impl;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.document.DocumentNotFoundException;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock private DocumentRepository documentRepository;

    @InjectMocks private DocumentServiceImpl documentService;

    private User client, pt;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").id(2L).firstName("Luca").lastName("Bianchi").role(Role.PERSONAL_TRAINER).build();
        client = User.builder().email("client@test.com").password("testpass").id(1L).firstName("Mario").lastName("Rossi").role(Role.CLIENT).build();
    }

    // ─── uploadDocument ───────────────────────────────────────────────────────

    @Test @DisplayName("uploadDocument — costruisce entity e salva")
    void uploadDocument_success() {
        String filePath = "/tmp/uploads/scheda.pdf";
        Document saved = Document.builder().id(1L).fileName("scheda.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.save(any())).thenReturn(saved);

        Document result = documentService.uploadDocument(filePath, "scheda.pdf", "application/pdf", "WORKOUT_PLAN", client, pt);
        assertThat(result.getFileName()).isEqualTo("scheda.pdf");
        assertThat(result.getType()).isEqualTo(DocumentType.WORKOUT_PLAN);
    }

    @Test @DisplayName("uploadDocument — tipo documento non valido lancia IllegalArgumentException")
    void uploadDocument_invalidDocType() {
        assertThatThrownBy(() -> documentService.uploadDocument(
                "/tmp/uploads/f.pdf", "f.pdf", "application/pdf", "TIPO_INESISTENTE", client, pt))
                .isInstanceOf(IllegalArgumentException.class);
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
        Document doc = Document.builder().id(1L).fileName("test.pdf").type(DocumentType.WORKOUT_PLAN)
                .owner(client).uploadedBy(pt).uploadDate(LocalDateTime.now()).build();
        when(documentRepository.findByOwnerOrderByUploadDateDesc(client)).thenReturn(List.of(doc));

        List<Document> result = documentService.getUserDocuments(client);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getUserDocumentsByType — filtrato per tipo")
    void getUserDocumentsByType() {
        when(documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(client, DocumentType.WORKOUT_PLAN))
                .thenReturn(List.of());

        List<Document> result = documentService.getUserDocumentsByType(client, "WORKOUT_PLAN");
        assertThat(result).isEmpty();
    }

    // ─── deleteDocument ───────────────────────────────────────────────────────

    @Test @DisplayName("deleteDocument — elimina dal db")
    void deleteDocument_success() {
        Document doc = Document.builder().id(1L).filePath("/tmp/uploads/to-delete.pdf").build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L);

        verify(documentRepository).delete(doc);
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
