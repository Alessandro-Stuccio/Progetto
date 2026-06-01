package com.project.tesi.mapper;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte i documenti nei rispettivi DTO di risposta.
 */
@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document doc) {
        if (doc == null) return null;
        return DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .contentType(doc.getContentType())
                .type(doc.getType().name())
                .uploadDate(doc.getUploadDate().toString())
                .notes(doc.getNotes())
                .uploadedByName(doc.getUploadedBy() != null ? doc.getUploadedBy().getFullName() : null)
                .build();
    }

    public List<DocumentResponse> toResponseList(List<Document> documents) {
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Risposta minimale dopo la modifica delle note: solo id e note aggiornate.
    public UpdatedNotesResponse toUpdatedNotesResponse(Document doc) {
        return UpdatedNotesResponse.builder().id(doc.getId()).notes(doc.getNotes()).build();
    }
}
