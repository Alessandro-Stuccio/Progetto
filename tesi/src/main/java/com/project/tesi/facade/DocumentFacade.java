package com.project.tesi.facade;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.DocumentUploadResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Facade per caricamento, download e gestione documenti con controllo accessi.
 */
public interface DocumentFacade {

    /**
     * Carica un documento con validazione del tipo e dei permessi.
     *
     * @param file       file da caricare
     * @param clientId   identificativo del cliente a cui appartiene il documento
     * @param uploaderId identificativo dell'utente che effettua il caricamento
     * @param type       tipo del documento
     * @return dettagli del documento caricato
     */
    DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type);

    /**
     * Recupera un documento tramite il suo identificativo.
     *
     * @param id identificativo del documento
     * @return entità documento corrispondente
     */
    Document getDocumentById(Long id);

    /**
     * Elimina un documento verificando che il chiamante ne sia il proprietario.
     *
     * @param id       identificativo del documento da eliminare
     * @param callerId identificativo dell'utente che richiede l'eliminazione
     */
    void deleteDocument(Long id, Long callerId);

    /**
     * Scarica il contenuto di un documento verificando i permessi di accesso.
     *
     * @param id       identificativo del documento da scaricare
     * @param callerId identificativo dell'utente che richiede il download
     * @return contenuto binario del documento
     */
    byte[] downloadDocumentSecure(Long id, Long callerId);

    /**
     * Restituisce i documenti di un utente con controllo degli accessi.
     *
     * @param targetUserId identificativo dell'utente di cui recuperare i documenti
     * @param callerId     identificativo dell'utente che effettua la richiesta
     * @return lista dei documenti dell'utente accessibili al chiamante
     */
    List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId);

    /**
     * Restituisce i documenti di un utente filtrati per tipo, con controllo degli accessi.
     *
     * @param targetUserId identificativo dell'utente di cui recuperare i documenti
     * @param type         tipo di documento da filtrare
     * @param callerId     identificativo dell'utente che effettua la richiesta
     * @return lista dei documenti del tipo specificato accessibili al chiamante
     */
    List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId);

    /**
     * Aggiorna le note associate a un documento.
     *
     * @param id       identificativo del documento da aggiornare
     * @param notes    nuovo testo delle note
     * @param callerId identificativo dell'utente che effettua la modifica
     * @return risposta con le note aggiornate
     */
    UpdatedNotesResponse updateNotes(Long id, String notes, Long callerId);
}
