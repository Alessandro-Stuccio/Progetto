package com.project.tesi.facade;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;

import java.util.List;

/**
 * Gestione delle recensioni dei professionisti.
 */
public interface ReviewFacade {

    /**
     * Registra una recensione lasciata dal cliente su un professionista.
     */
    ReviewResponse addReview(ReviewRequest request, Long userId);

    /**
     * Tutte le recensioni ricevute dal professionista.
     */
    List<ReviewResponse> getReviewsForProfessional(Long professionalId);

    /**
     * Indica se il cliente può recensire: serve almeno una prenotazione passata con quel professionista.
     */
    boolean canClientReview(Long clientId, Long professionalId);

    /**
     * Indica se il cliente ha già recensito quel professionista (una sola recensione per coppia).
     */
    boolean hasClientReviewed(Long clientId, Long professionalId);
}
