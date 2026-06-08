package com.project.kore.dto.response;

/**
 * Esito dell'aggiornamento delle note di un documento.
 */
public class UpdatedNotesResponse {

    private Long id;
    private String notes;


    private UpdatedNotesResponse(Builder b) {
        this.id = b.id;
        this.notes = b.notes;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getNotes() { return notes; }

    public static class Builder {
        private Long id;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public UpdatedNotesResponse build() { return new UpdatedNotesResponse(this); }
    }
}
