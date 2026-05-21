package com.project.tesi.service;

import com.project.tesi.model.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaccia del servizio per la gestione dei documenti.
 * Gestisce caricamento, download, eliminazione e aggiornamento note
 * di file associati ai clienti (schede, piani alimentari, certificati, ecc.).
 */
@Validated
public interface DocumentService {

    /** Carica un documento validando il ruolo dell'uploader rispetto al tipo di file. */
    DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String docType);

    /** Scarica il contenuto binario di un documento. */
    byte[] downloadDocument(Long documentId);

    /** Recupera un documento per ID. */
    Document getDocumentById(Long documentId);

    /** Restituisce tutti i documenti di un utente come lista di DTO. */
    List<DocumentResponse> getUserDocumentsDto(Long userId);

    /** Restituisce i documenti di un utente filtrati per tipo come lista di DTO. */
    List<DocumentResponse> getUserDocumentsByTypeDto(Long userId, String docType);

    /** Elimina un documento dal database. */
    void deleteDocument(Long documentId);

    /** Aggiorna le note testuali di un documento. */
    UpdatedNotesResponse updateNotes(Long documentId, String notes);

    /** Converte un'entità Document nel DTO per la risposta. */
    DocumentResponse toDto(Document doc);

    /** Salva un'entità Document nel database. */
    Document saveDocument(Document document);

    /** Recupera tutti i documenti associati a un utente (entità). */
    List<Document> getUserDocuments(Long userId);

    /** Recupera i documenti associati a un utente filtrati per tipo (entità). */
    List<Document> getUserDocumentsByType(Long userId, String docType);

    /** Carica un documento senza validazione del ruolo (operazione base). */
    Document uploadDocument(MultipartFile file, Long clientId, Long uploaderId, String docType);

    List<Document> findRecentByOwner(@NotNull User owner,@NotNull @PastOrPresent LocalDateTime since);


    List<Document> findRecentByProfessional(User professional, LocalDateTime since);

}