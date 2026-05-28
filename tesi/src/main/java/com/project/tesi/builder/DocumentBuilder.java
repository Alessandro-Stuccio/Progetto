package com.project.tesi.builder;

import com.project.tesi.enums.DocumentType;
import java.time.LocalDateTime;
import com.project.tesi.model.*;


/**
 * Builder per l'entità Document. Fluent interface per la costruzione dei metadati di un documento caricato.
 */
public interface DocumentBuilder {
    DocumentBuilder id(Long id);
    DocumentBuilder fileName(String fileName);
    DocumentBuilder filePath(String filePath);
    DocumentBuilder contentType(String contentType);
    DocumentBuilder type(DocumentType type);
    DocumentBuilder owner(User owner);
    DocumentBuilder uploadedBy(User uploadedBy);
    DocumentBuilder uploadDate(LocalDateTime uploadDate);
    DocumentBuilder notes(String notes);
    Document build();
}
