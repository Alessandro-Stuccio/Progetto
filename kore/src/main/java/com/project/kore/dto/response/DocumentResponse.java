package com.project.kore.dto.response;

/**
 * Metadati di un documento, senza i dati binari del file.
 */
public class DocumentResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private String type;
    private String uploadDate;
    private String notes;
    private String uploadedByName;

    private DocumentResponse(Builder b) {
        this.id = b.id;
        this.fileName = b.fileName;
        this.contentType = b.contentType;
        this.type = b.type;
        this.uploadDate = b.uploadDate;
        this.notes = b.notes;
        this.uploadedByName = b.uploadedByName;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public String getType() { return type; }
    public String getUploadDate() { return uploadDate; }
    public String getNotes() { return notes; }
    public String getUploadedByName() { return uploadedByName; }

    public static class Builder {
        private Long id;
        private String fileName;
        private String contentType;
        private String type;
        private String uploadDate;
        private String notes;
        private String uploadedByName;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder uploadDate(String uploadDate) { this.uploadDate = uploadDate; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder uploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; return this; }

        public DocumentResponse build() { return new DocumentResponse(this); }
    }
}
