package com.project.tesi.controller;

import com.project.tesi.dto.request.UpdateNotesRequest;
import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Gestione documenti. L'upload viaggia come form-data con il file binario in MultipartFile. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentFacade documentFacade;

    public DocumentController(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }

    /** Carica un documento validando il ruolo dell'uploader rispetto al tipo di file. */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam("type") String type,
            @AuthenticationPrincipal User uploader) {
        return ResponseEntity.ok(documentFacade.uploadDocumentWithValidation(file, clientId, uploader.getId(), type));
    }

    /** Scarica il contenuto binario di un documento per la visualizzazione inline nel browser. */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
                                               @AuthenticationPrincipal User caller) {
        Document doc = documentFacade.getDocumentById(id);
        byte[] data = documentFacade.downloadDocumentSecure(id, caller.getId());
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    /** Restituisce tutti i documenti di un utente (qualsiasi tipo). */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> getUserDocuments(@PathVariable Long userId,
                                                                    @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.getUserDocumentsDtoSecure(userId, caller.getId()));
    }

    /** Restituisce i documenti di un utente filtrati per tipologia. */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<DocumentResponse>> getUserDocumentsByType(
            @PathVariable Long userId, @PathVariable String type,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.getUserDocumentsByTypeDtoSecure(userId, type, caller.getId()));
    }

    /** Elimina un documento dal database e dal filesystem. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               @AuthenticationPrincipal User caller) {
        documentFacade.deleteDocument(id, caller.getId());
        return ResponseEntity.noContent().build();
    }

    /** Aggiorna le note testuali associate a un documento. Solo proprietario, uploader o admin/moderatore. */
    @PutMapping("/{id}/notes")
    public ResponseEntity<UpdatedNotesResponse> updateNotes(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotesRequest body,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.updateNotes(id, body.notes(), caller.getId()));
    }
}
