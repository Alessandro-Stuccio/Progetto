package com.project.tesi.service;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio per la gestione dei documenti caricati dagli utenti.
 */
@Validated
public interface DocumentService {

    /**
     * Recupera un documento tramite il suo identificativo.
     *
     * @param documentId identificativo del documento
     * @return documento corrispondente
     */
    Document getDocumentById(@NotNull @Min(1) Long documentId);

    /**
     * Elimina un documento dal sistema.
     *
     * @param documentId identificativo del documento da eliminare
     */
    void deleteDocument(@NotNull @Min(1) Long documentId);

    /**
     * Aggiorna le note testuali associate a un documento.
     *
     * @param documentId identificativo del documento
     * @param notes      nuovo testo delle note
     * @return documento aggiornato
     */
    Document updateNotes(@NotNull @Min(1) Long documentId, @NotBlank String notes);

    /**
     * Persiste un'entità documento nel database.
     *
     * @param document entità da salvare
     * @return documento salvato
     */
    Document saveDocument(@NotNull Document document);

    /**
     * Restituisce tutti i documenti appartenenti a un utente.
     *
     * @param owner utente proprietario dei documenti
     * @return lista dei documenti dell'utente
     */
    List<Document> getUserDocuments(@NotNull User owner);

    /**
     * Restituisce i documenti di un utente filtrati per tipo.
     *
     * @param owner   utente proprietario dei documenti
     * @param docType stringa rappresentante il tipo di documento
     * @return lista dei documenti del tipo specificato
     */
    List<Document> getUserDocumentsByType(@NotNull User owner, @NotBlank String docType);

    /**
     * Crea e persiste un documento a partire dai metadati e dal file già archiviato.
     *
     * @param filePath     percorso del file sul filesystem
     * @param originalName nome originale del file
     * @param contentType  MIME type del file
     * @param docType      tipo di documento
     * @param client       utente proprietario del documento
     * @param uploader     utente che ha effettuato il caricamento
     * @return documento creato e salvato
     */
    Document uploadDocument(@NotBlank String filePath,
                            @NotBlank String originalName,
                            String contentType,
                            @NotBlank String docType,
                            @NotNull User client,
                            @NotNull User uploader);

    /**
     * Restituisce i documenti recenti di un utente a partire da una data.
     *
     * @param owner utente proprietario
     * @param since data/ora di inizio ricerca (passata o presente)
     * @return lista dei documenti caricati da {@code since}
     */
    List<Document> findRecentByOwner(@NotNull User owner, @NotNull @PastOrPresent LocalDateTime since);

    /**
     * Restituisce i documenti recenti caricati da un professionista a partire da una data.
     *
     * @param professional utente professionista
     * @param since        data/ora di inizio ricerca
     * @return lista dei documenti caricati dal professionista da {@code since}
     */
    List<Document> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /**
     * Recupera l'ultimo documento caricato per un utente e un tipo specifico.
     *
     * @param owner utente proprietario
     * @param type  tipo di documento
     * @return documento più recente del tipo specificato
     */
    Document findLatestByOwnerAndType(@NotNull User owner, @NotNull DocumentType type);

    /**
     * Conta il numero di documenti caricati da un professionista a partire da una data.
     *
     * @param professional utente professionista
     * @param since        data/ora di inizio conteggio
     * @return numero di documenti caricati da {@code since}
     */
    int countUploadedSince(@NotNull User professional, @NotNull LocalDateTime since);
}
