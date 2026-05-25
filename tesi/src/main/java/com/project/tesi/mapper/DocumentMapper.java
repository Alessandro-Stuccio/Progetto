package com.project.tesi.mapper;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document doc) {
        if (doc == null) return null;
        return new DocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getType().name(),
                doc.getUploadDate().toString(),
                doc.getOwner().getId(),
                doc.getOwner().getFullName(),
                doc.getUploadedBy().getId(),
                doc.getUploadedBy().getFullName(),
                doc.getNotes()
        );
    }

    public List<DocumentResponse> toResponseList(List<Document> documents) {
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UpdatedNotesResponse toUpdatedNotesResponse(Document doc) {
        return new UpdatedNotesResponse(doc.getId(), doc.getNotes());
    }
}
