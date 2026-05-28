package com.project.tesi.mapper;

import com.project.tesi.dto.response.DocumentResponse;
import com.project.tesi.dto.response.UpdatedNotesResponse;
import com.project.tesi.model.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione di {@link Document} in DTO di risposta.
 */
@Component
public class DocumentMapper {

    /**
     * Converte un {@link Document} in {@link DocumentResponse}.
     *
     * @param doc il documento da convertire
     * @return il DTO di risposta, o {@code null} se il documento è {@code null}
     */
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

    /**
     * Converte una lista di {@link Document} in una lista di {@link DocumentResponse}.
     *
     * @param documents lista dei documenti
     * @return lista dei DTO di risposta
     */
    public List<DocumentResponse> toResponseList(List<Document> documents) {
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Crea un {@link UpdatedNotesResponse} con l'ID e le note aggiornate del documento.
     *
     * @param doc il documento con le note già modificate
     * @return il DTO contenente ID e note aggiornate
     */
    public UpdatedNotesResponse toUpdatedNotesResponse(Document doc) {
        return UpdatedNotesResponse.builder().id(doc.getId()).notes(doc.getNotes()).build();
    }
}
