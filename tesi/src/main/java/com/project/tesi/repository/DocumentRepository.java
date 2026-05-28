package com.project.tesi.repository;

import com.project.tesi.enums.DocumentType;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository per l'accesso ai dati dell'entità {@link Document}.
 *
 * Fornisce query per recuperare i documenti filtrati per proprietario,
 * tipo, uploader e intervallo temporale.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Restituisce tutti i documenti di un cliente (qualsiasi tipo).
     *
     * @param owner il cliente proprietario
     * @return lista dei documenti
     */
    List<Document> findByOwner(User owner);

    /**
     * Restituisce i documenti di un cliente filtrati per tipo.
     *
     * @param owner il cliente proprietario
     * @param type  il tipo di documento
     * @return lista dei documenti corrispondenti
     */
    List<Document> findByOwnerAndType(User owner, DocumentType type);

    /**
     * Restituisce tutti i documenti di un cliente ordinati per data di caricamento decrescente.
     *
     * @param owner il cliente proprietario
     * @return lista dei documenti (dal più recente)
     */
    List<Document> findByOwnerOrderByUploadDateDesc(User owner);

    /**
     * Restituisce i documenti di un cliente filtrati per tipo,
     * ordinati per data di caricamento decrescente.
     *
     * @param owner il cliente proprietario
     * @param type  il tipo di documento
     * @return lista dei documenti corrispondenti (dal più recente)
     */
    List<Document> findByOwnerAndTypeOrderByUploadDateDesc(User owner, DocumentType type);

    /**
     * Conta i documenti caricati da un professionista dopo una certa data.
     * Usato nella dashboard del professionista per le statistiche settimanali.
     *
     * @param uploader il professionista che ha caricato i documenti
     * @param since    data/ora minima di caricamento
     * @return numero di documenti caricati nel periodo
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.uploadedBy = :uploader AND d.uploadDate >= :since")
    int countByUploaderSince(@Param("uploader") User uploader, @Param("since") LocalDateTime since);

    /**
     * Trova l'ultimo documento di un certo tipo caricato per un cliente.
     * Usato per verificare se un cliente necessita di un aggiornamento
     * (es. scheda scaduta da più di 7 giorni).
     *
     * @param owner il cliente proprietario
     * @param type  il tipo di documento
     * @return l'ultimo documento trovato, oppure {@code null} se non esiste
     */
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.type = :type ORDER BY d.uploadDate DESC LIMIT 1")
    Document findLatestByOwnerAndType(@Param("owner") User owner, @Param("type") DocumentType type);

    /**
     * Restituisce i documenti recenti caricati per un cliente (per il feed attività).
     *
     * @param owner il cliente proprietario
     * @param since data/ora minima di caricamento
     * @return lista dei documenti recenti
     */
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.uploadDate >= :since ORDER BY d.uploadDate DESC")
    List<Document> findRecentByOwner(@Param("owner") User owner, @Param("since") LocalDateTime since);

    /**
     * Restituisce i documenti recenti caricati da un professionista (per il feed attività).
     *
     * @param uploader il professionista che ha caricato
     * @param since    data/ora minima di caricamento
     * @return lista dei documenti recenti
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedBy = :uploader AND d.uploadDate >= :since ORDER BY d.uploadDate DESC")
    List<Document> findRecentByUploader(@Param("uploader") User uploader, @Param("since") LocalDateTime since);

}