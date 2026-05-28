package com.project.tesi.controller;

import com.project.tesi.dto.request.UpdateNotesRequest;
import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.InsuranceFacade;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST per l'Insurance Manager.
 * Espone /api/insurance. Richiede ruolo INSURANCE_MANAGER.
 */
@RestController
@RequestMapping("/api/insurance")
@Tag(name = "Insurance", description = "API riservate all'Insurance Manager")
public class InsuranceController {

    private final InsuranceFacade insuranceFacade;

    public InsuranceController(InsuranceFacade insuranceFacade) {
        this.insuranceFacade = insuranceFacade;
    }

    /**
     * Recupera tutti gli abbonamenti presenti nel sistema (attivi e scaduti).
     *
     * @return lista di {@link SubscriptionResponse}
     */
    @Operation(summary = "Lista abbonamenti", description = "Restituisce tutti gli abbonamenti attivi e scaduti.")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(insuranceFacade.getAllSubscriptions());
    }

    /**
     * Recupera admin e moderatori contattabili dall'insurance manager tramite chat.
     *
     * @return lista di {@link UserResponse}
     */
    @Operation(summary = "Contatti chat", description = "Restituisce admin e moderatori contattabili dall'insurance manager.")
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(insuranceFacade.getChatContacts());
    }

    /**
     * Recupera tutti i clienti registrati nel sistema.
     *
     * @return lista di {@link UserResponse}
     */
    @Operation(summary = "Lista clienti", description = "Restituisce tutti i clienti registrati.")
    @GetMapping("/clients")
    public ResponseEntity<List<UserResponse>> getClients() {
        return ResponseEntity.ok(insuranceFacade.getAllClients());
    }

    /**
     * Carica una polizza assicurativa associandola a un cliente specifico.
     *
     * @param clientId identificativo del cliente destinatario
     * @param file     file della polizza da caricare
     * @param caller   insurance manager autenticato che esegue il caricamento
     * @return {@link DocumentUploadResponse} con i metadati del documento salvato
     */
    @Operation(summary = "Carica polizza assicurativa per un cliente")
    @PostMapping("/clients/{clientId}/policy")
    public ResponseEntity<DocumentUploadResponse> uploadPolicy(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(insuranceFacade.uploadPolicy(file, clientId, caller.getId()));
    }

    /**
     * Recupera tutte le polizze assicurative associate a un cliente.
     *
     * @param clientId identificativo del cliente
     * @return lista di {@link DocumentResponse}
     */
    @Operation(summary = "Lista polizze di un cliente")
    @GetMapping("/clients/{clientId}/policies")
    public ResponseEntity<List<DocumentResponse>> getClientPolicies(@PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceFacade.getClientPolicies(clientId));
    }

    /**
     * Scarica il file di una polizza assicurativa tramite il suo identificativo.
     * Restituisce i byte del file con content-type e header di disposizione appropriati.
     *
     * @param id identificativo della polizza da scaricare
     * @return byte[] del file con header HTTP corretti per il download inline
     */
    @Operation(summary = "Scarica una polizza assicurativa")
    @GetMapping("/policies/{id}/download")
    public ResponseEntity<byte[]> downloadPolicy(@PathVariable Long id) {
        Document doc = insuranceFacade.getDocumentById(id);
        byte[] data = insuranceFacade.downloadPolicy(id);
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    /**
     * Elimina una polizza assicurativa e il file associato dal filesystem.
     *
     * @param id identificativo della polizza da eliminare
     * @return risposta HTTP 204 No Content
     */
    @Operation(summary = "Elimina una polizza assicurativa")
    @DeleteMapping("/policies/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        insuranceFacade.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggiorna le note testuali di una polizza assicurativa.
     *
     * @param id   identificativo della polizza
     * @param body oggetto contenente il testo delle nuove note
     * @return {@link UpdatedNotesResponse} con le note aggiornate
     */
    @Operation(summary = "Aggiorna le note di una polizza assicurativa")
    @PutMapping("/policies/{id}/notes")
    public ResponseEntity<UpdatedNotesResponse> updatePolicyNotes(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotesRequest body) {
        return ResponseEntity.ok(insuranceFacade.updatePolicyNotes(id, body.notes()));
    }
}
