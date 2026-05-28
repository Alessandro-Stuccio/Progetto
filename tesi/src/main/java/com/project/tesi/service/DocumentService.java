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

@Validated
public interface DocumentService {

    Document getDocumentById(@NotNull @Min(1) Long documentId);

    void deleteDocument(@NotNull @Min(1) Long documentId);

    Document updateNotes(@NotNull @Min(1) Long documentId, @NotBlank String notes);

    Document saveDocument(@NotNull Document document);

    List<Document> getUserDocuments(@NotNull User owner);

    List<Document> getUserDocumentsByType(@NotNull User owner, @NotBlank String docType);

    Document uploadDocument(@NotBlank String filePath,
                            @NotBlank String originalName,
                            String contentType,
                            @NotBlank String docType,
                            @NotNull User client,
                            @NotNull User uploader);

    List<Document> findRecentByOwner(@NotNull User owner, @NotNull @PastOrPresent LocalDateTime since);

    List<Document> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    Document findLatestByOwnerAndType(@NotNull User owner, @NotNull DocumentType type);

    int countUploadedSince(@NotNull User professional, @NotNull LocalDateTime since);
}
