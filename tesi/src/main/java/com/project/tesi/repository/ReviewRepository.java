package com.project.tesi.repository;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository per l'accesso ai dati dell'entità {@link Review}.
 *
 * Fornisce query per verificare l'esistenza di recensioni, recuperarle
 * per professionista e calcolare la media dei voti direttamente nel database.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Verifica se un cliente ha già recensito un professionista.
     * Usato per rispettare il vincolo di unicità (una sola recensione per coppia).
     *
     * @param clientId       ID del cliente
     * @param professionalId ID del professionista
     * @return {@code true} se la recensione esiste già
     */
    boolean existsByClientIdAndProfessionalId(Long clientId, Long professionalId);

    /**
     * Calcola la media dei voti (rating) di un professionista direttamente nel database.
     * Restituisce {@code null} se il professionista non ha ancora ricevuto recensioni.
     *
     * @param profId ID del professionista
     * @return la media dei voti, oppure {@code null}
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.professional.id = :profId")
    Double getAverageRating(@Param("profId") Long profId);

    /**
     * Restituisce tutte le recensioni ricevute da un professionista (per entità User).
     *
     * @param professional il professionista
     * @return lista delle recensioni
     */
    List<Review> findByProfessional(User professional);

}