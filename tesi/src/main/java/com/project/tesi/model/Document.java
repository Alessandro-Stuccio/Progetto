package com.project.tesi.model;

import com.project.tesi.builder.DocumentBuilder;
import com.project.tesi.builder.impl.DocumentBuilderImpl;
import com.project.tesi.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entità JPA per un documento caricato sulla piattaforma.
 *
 * <p>Il file fisico è salvato sul filesystem nella directory {@code uploads/};
 * questa entità ne traccia esclusivamente i metadati (nome, percorso, MIME type, tipo logico).
 *
 * <p>Relazioni chiave:
 * <ul>
 *   <li>{@code owner} — il cliente a cui appartiene il documento.</li>
 *   <li>{@code uploadedBy} — chi ha materialmente caricato il file; può essere il professionista
 *       (personal trainer o nutrizionista) oppure un insurance manager, non necessariamente il cliente.</li>
 * </ul>
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome originale del file così come fornito al momento del caricamento. */
    private String fileName;

    /** Percorso fisico assoluto (o relativo alla root dell'applicazione) del file sul filesystem. */
    private String filePath;

    /** MIME type del file (es. {@code application/pdf}, {@code image/png}). */
    private String contentType;

    /** Tipo logico del documento secondo l'enum {@code DocumentType} (es. referto, contratto, ecc.). */
    @Enumerated(EnumType.STRING)
    private DocumentType type;

    /** Cliente proprietario del documento; destinatario logico del file. */
    @ManyToOne
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_document_owner_id"))
    private User owner;

    /**
     * Utente che ha effettuato il caricamento fisico del file.
     * Distinto da {@code owner}: può essere un professionista o un insurance manager.
     */
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id", foreignKey = @ForeignKey(name = "fk_document_uploaded_by_id"))
    private User uploadedBy;

    /** Data e ora in cui il documento è stato caricato sulla piattaforma. */
    private LocalDateTime uploadDate;

    /** Note testuali libere associate al documento; mappato come {@code TEXT} per supportare contenuti lunghi. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    public Document() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static DocumentBuilder builder() {
        return new DocumentBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document that = (Document) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ", fileName='" + fileName + "', type=" + type + ", uploadDate=" + uploadDate + "}";
    }
}
