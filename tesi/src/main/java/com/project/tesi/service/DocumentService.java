package com.project.tesi.service;

import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface DocumentService {

    byte[] downloadDocument(@NotNull @Min(1) Long documentId);

    Document getDocumentById(@NotNull @Min(1) Long documentId);

    void deleteDocument(@NotNull @Min(1) Long documentId);

    Document updateNotes(@NotNull @Min(1) Long documentId, @NotBlank String notes);

    Document saveDocument(@NotNull Document document);

    List<Document> getUserDocuments(@NotNull @Min(1) Long userId);

    List<Document> getUserDocumentsByType(@NotNull @Min(1) Long userId, @NotBlank String docType);

    Document uploadDocument(@NotNull MultipartFile file,
                            @NotNull @Min(1) Long clientId,
                            @NotNull @Min(1) Long uploaderId,
                            @NotBlank String docType);

    List<Document> findRecentByOwner(@NotNull User owner, @NotNull @PastOrPresent LocalDateTime since);

    List<Document> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);
}
