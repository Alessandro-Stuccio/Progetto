package com.project.tesi.service;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Servizio per la gestione delle recensioni dei professionisti.
 */
@Validated
public interface ReviewService {

    /**
     * Persiste una recensione nel database.
     *
     * @param review entità recensione da salvare
     * @return recensione salvata
     */
    Review save(@NotNull Review review);

    /**
     * Verifica se un cliente ha già lasciato una recensione a un professionista.
     *
     * @param clientId       identificativo del cliente
     * @param professionalId identificativo del professionista
     * @return {@code true} se la recensione esiste, {@code false} altrimenti
     */
    boolean existsByClientAndProfessional(@NotNull Long clientId, @NotNull Long professionalId);

    /**
     * Restituisce tutte le recensioni ricevute da un professionista.
     *
     * @param professional utente professionista
     * @return lista delle recensioni del professionista
     */
    List<Review> findByProfessional(@NotNull User professional);

    /**
     * Calcola la valutazione media di un professionista.
     *
     * @param professionalId identificativo del professionista
     * @return media dei voti ricevuti
     */
    double getAverageRating(@NotNull Long professionalId);
}
