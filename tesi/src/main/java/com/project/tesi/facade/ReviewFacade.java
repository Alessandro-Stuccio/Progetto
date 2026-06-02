package com.project.tesi.facade;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.review.ReviewNotAllowedException;

import java.util.List;

/**
 * Gestione delle recensioni dei professionisti.
 */
public interface ReviewFacade {

    /**
     * Registra una recensione lasciata dal cliente su un professionista.
     *
     * @param request dati della recensione (professionista, voto, testo)
     * @param userId  id del cliente che recensisce
     * @return la recensione registrata
     * @throws ResourceAlreadyExistsException se il cliente ha già recensito quel professionista
     * @throws ReviewNotAllowedException       se il cliente non ha alcuna prenotazione con quel professionista
     */
    ReviewResponse addReview(ReviewRequest request, Long userId);

    /**
     * Tutte le recensioni ricevute dal professionista.
     *
     * @param professionalId id del professionista
     * @return le sue recensioni
     */
    List<ReviewResponse> getReviewsForProfessional(Long professionalId);

    /**
     * Indica se il cliente può recensire: serve almeno una prenotazione passata con quel professionista.
     *
     * @param clientId       id del cliente
     * @param professionalId id del professionista
     * @return {@code true} se il cliente è autorizzato a recensire
     */
    boolean canClientReview(Long clientId, Long professionalId);

    /**
     * Indica se il cliente ha già recensito quel professionista (una sola recensione per coppia).
     *
     * @param clientId       id del cliente
     * @param professionalId id del professionista
     * @return {@code true} se esiste già una recensione del cliente per quel professionista
     */
    boolean hasClientReviewed(Long clientId, Long professionalId);
}
