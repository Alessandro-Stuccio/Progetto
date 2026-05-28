package com.project.tesi.facade;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;

import java.util.List;

/**
 * Facade per la gestione delle recensioni dei professionisti.
 */
public interface ReviewFacade {

    /**
     * Aggiunge una nuova recensione per un professionista.
     *
     * @param request dati della recensione da aggiungere
     * @param userId  identificativo del cliente che lascia la recensione
     * @return dettagli della recensione creata
     */
    ReviewResponse addReview(ReviewRequest request, Long userId);

    /**
     * Restituisce tutte le recensioni ricevute da un professionista.
     *
     * @param professionalId identificativo del professionista
     * @return lista delle recensioni del professionista
     */
    List<ReviewResponse> getReviewsForProfessional(Long professionalId);

    /**
     * Verifica se il cliente ha almeno una prenotazione passata con il professionista,
     * condizione necessaria per poter lasciare una recensione.
     *
     * @param clientId       identificativo del cliente
     * @param professionalId identificativo del professionista
     * @return {@code true} se il cliente può recensire, {@code false} altrimenti
     */
    boolean canClientReview(Long clientId, Long professionalId);

    /**
     * Verifica se il cliente ha già recensito il professionista specificato.
     *
     * @param clientId       identificativo del cliente
     * @param professionalId identificativo del professionista
     * @return {@code true} se la recensione è già stata lasciata, {@code false} altrimenti
     */
    boolean hasClientReviewed(Long clientId, Long professionalId);
}
