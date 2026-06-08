package com.project.kore.dto.response;

/**
 * Esito del caricamento di un documento: ID assegnato e nome del file salvato.
 */
public class DocumentUploadResponse {

    private Long id;
    private String fileName;
    private String type;
    private String uploadDate;


    private DocumentUploadResponse(Builder b) {
        this.id = b.id;
        this.fileName = b.fileName;
        this.type = b.type;
        this.uploadDate = b.uploadDate;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getType() { return type; }
    public String getUploadDate() { return uploadDate; }

    public static class Builder {
        private Long id;
        private String fileName;
        private String type;
        private String uploadDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder uploadDate(String uploadDate) { this.uploadDate = uploadDate; return this; }

        public DocumentUploadResponse build() { return new DocumentUploadResponse(this); }
    }
}
