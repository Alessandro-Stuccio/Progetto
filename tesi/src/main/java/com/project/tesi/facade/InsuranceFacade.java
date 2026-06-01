package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Operazioni dell'Insurance Manager: gestione polizze e visibilità sui clienti.
 */
public interface InsuranceFacade {

    /**
     * Tutti i clienti registrati.
     */
    List<UserResponse> getAllClients();

    /**
     * Tutti gli abbonamenti, attivi e storici.
     */
    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Utenti che l'Insurance Manager può contattare in chat.
     */
    List<UserResponse> getChatContacts();

    Document getDocumentById(Long documentId);

    /**
     * Carica una polizza per un cliente per conto dell'Insurance Manager.
     */
    DocumentUploadResponse uploadPolicy(MultipartFile file, Long clientId, Long callerId);

    byte[] downloadPolicy(Long documentId);

    void deletePolicy(Long documentId);

    /**
     * Le polizze associate a un cliente.
     */
    List<DocumentResponse> getClientPolicies(Long clientId);

    /**
     * Aggiorna le note di una polizza.
     */
    UpdatedNotesResponse updatePolicyNotes(Long documentId, String notes);
}
