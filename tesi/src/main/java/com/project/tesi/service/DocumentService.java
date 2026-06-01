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

/** Gestione dei documenti caricati dagli utenti. */
@Validated
public interface DocumentService {

    Document getDocumentById(@NotNull @Min(1) Long documentId);

    void deleteDocument(@NotNull @Min(1) Long documentId);

    /** Aggiorna le note testuali del documento. */
    Document updateNotes(@NotNull @Min(1) Long documentId, @NotBlank String notes);

    Document saveDocument(@NotNull Document document);

    List<Document> getUserDocuments(@NotNull User owner);

    /** Documenti dell'utente filtrati per tipo. */
    List<Document> getUserDocumentsByType(@NotNull User owner, @NotBlank String docType);

    /** Crea il record del documento partendo dai metadati e dal file già archiviato su disco. */
    Document uploadDocument(@NotBlank String filePath,
                            @NotBlank String originalName,
                            String contentType,
                            @NotBlank String docType,
                            @NotNull User client,
                            @NotNull User uploader);

    /** Documenti di un utente caricati a partire dalla data indicata. */
    List<Document> findRecentByOwner(@NotNull User owner, @NotNull @PastOrPresent LocalDateTime since);

    /** Documenti caricati da un professionista a partire dalla data indicata. */
    List<Document> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /** L'ultimo documento di un certo tipo per quell'utente. */
    Document findLatestByOwnerAndType(@NotNull User owner, @NotNull DocumentType type);

    /** Quanti documenti ha caricato il professionista dalla data indicata. */
    int countUploadedSince(@NotNull User professional, @NotNull LocalDateTime since);
}
