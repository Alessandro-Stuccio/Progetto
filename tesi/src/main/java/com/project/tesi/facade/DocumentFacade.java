package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Upload, download e gestione documenti, con controllo accessi su ogni operazione.
 */
public interface DocumentFacade {

    /**
     * Carica un documento per un cliente, dopo aver validato tipo e permessi dell'uploader.
     */
    DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type);

    Document getDocumentById(Long id);

    /**
     * Elimina il documento solo se il chiamante ne è il proprietario.
     */
    void deleteDocument(Long id, Long callerId);

    /**
     * Scarica il contenuto del documento dopo aver verificato che il chiamante possa accedervi.
     */
    byte[] downloadDocumentSecure(Long id, Long callerId);

    /**
     * Restituisce i documenti dell'utente target visibili al chiamante.
     */
    List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId);

    /**
     * Come getUserDocumentsDtoSecure, ma filtrando per tipo di documento.
     */
    List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId);

    /**
     * Aggiorna le note del documento, verificando i permessi del chiamante.
     */
    UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId);
}
