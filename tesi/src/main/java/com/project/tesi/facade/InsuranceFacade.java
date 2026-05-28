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
 * Facade per le operazioni dell'Insurance Manager: gestione polizze e visibilità clienti.
 */
public interface InsuranceFacade {

    /**
     * Restituisce la lista di tutti i clienti registrati.
     *
     * @return lista di tutti i clienti
     */
    List<UserResponse> getAllClients();

    /**
     * Restituisce la lista di tutti gli abbonamenti attivi e storici.
     *
     * @return lista di tutti gli abbonamenti
     */
    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Restituisce i contatti disponibili per la chat dell'Insurance Manager.
     *
     * @return lista degli utenti contattabili
     */
    List<UserResponse> getChatContacts();

    /**
     * Recupera un documento tramite il suo identificativo.
     *
     * @param documentId identificativo del documento
     * @return entità documento corrispondente
     */
    Document getDocumentById(Long documentId);

    /**
     * Carica una polizza assicurativa per un cliente.
     *
     * @param file     file della polizza da caricare
     * @param clientId identificativo del cliente destinatario
     * @param callerId identificativo dell'Insurance Manager che esegue il caricamento
     * @return dettagli del documento polizza caricato
     */
    DocumentUploadResponse uploadPolicy(MultipartFile file, Long clientId, Long callerId);

    /**
     * Scarica il contenuto di una polizza assicurativa.
     *
     * @param documentId identificativo della polizza da scaricare
     * @return contenuto binario della polizza
     */
    byte[] downloadPolicy(Long documentId);

    /**
     * Elimina una polizza assicurativa.
     *
     * @param documentId identificativo della polizza da eliminare
     */
    void deletePolicy(Long documentId);

    /**
     * Restituisce le polizze assicurative associate a un cliente.
     *
     * @param clientId identificativo del cliente
     * @return lista delle polizze del cliente
     */
    List<DocumentResponse> getClientPolicies(Long clientId);

    /**
     * Aggiorna le note associate a una polizza assicurativa.
     *
     * @param documentId identificativo della polizza da aggiornare
     * @param notes      nuovo testo delle note
     * @return risposta con le note aggiornate
     */
    UpdatedNotesResponse updatePolicyNotes(Long documentId, String notes);
}
