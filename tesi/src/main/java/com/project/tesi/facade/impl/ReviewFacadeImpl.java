package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.review.ReviewNotAllowedException;
import com.project.tesi.facade.ReviewFacade;
import com.project.tesi.mapper.ReviewMapper;
import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.service.ReviewService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementazione di {@link ReviewFacade}.
 * Verifica i prerequisiti per la recensione (prenotazione effettuata,
 * recensione non già presente) prima del salvataggio.
 */
@Component
public class ReviewFacadeImpl implements ReviewFacade {

    private final UserService userService;
    private final ReviewService reviewService;
    private final SlotService slotService;
    private final ReviewMapper reviewMapper;

    public ReviewFacadeImpl(UserService userService, ReviewService reviewService,
                            SlotService slotService, ReviewMapper reviewMapper) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.slotService = slotService;
        this.reviewMapper = reviewMapper;
    }

    /**
     * Verifica che il cliente non abbia già recensito il professionista e che
     * abbia un rapporto formale con esso (prenotazione effettuata o assegnazione attiva),
     * quindi costruisce e salva la {@link Review}.
     *
     * @param request dati della recensione (professionalId, rating, commento)
     * @param userId  identificatore del cliente che lascia la recensione
     * @return {@link ReviewResponse} con i dati della recensione salvata
     * @throws ResourceAlreadyExistsException se il cliente ha già recensito il professionista
     * @throws ReviewNotAllowedException      se il cliente non ha un rapporto formale col professionista
     */
    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        User user = userService.getUserById(userId);
        User professional = userService.getUserById(request.professionalId());

        if (reviewService.existsByClientAndProfessional(user.getId(), professional.getId())) {
            throw new ResourceAlreadyExistsException("Hai già lasciato una recensione per questo professionista.");
        }
        if (!this.canClientReview(user.getId(), professional.getId())) {
            throw new ReviewNotAllowedException(
                    "Puoi recensire solo professionisti con cui hai avuto un rapporto formale.");
        }

        Review review = Review.builder()
                .client(user)
                .professional(professional)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return reviewMapper.toResponse(reviewService.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return reviewMapper.toResponseList(reviewService.findByProfessional(professional));
    }

    /**
     * Verifica se il cliente può recensire il professionista.
     * Restituisce {@code false} se ha già recensito; {@code true} se ha almeno
     * una prenotazione completata o se è attualmente assegnato al professionista.
     *
     * @param clientId       identificatore del cliente
     * @param professionalId identificatore del professionista
     * @return {@code true} se la recensione è consentita, {@code false} altrimenti
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canClientReview(Long clientId, Long professionalId) {
        if (reviewService.existsByClientAndProfessional(clientId, professionalId)) return false;
        if (slotService.hasBookingBetween(clientId, professionalId)) return true;
        User client = userService.getUserById(clientId);
        User professional = userService.getUserById(professionalId);
        return isCurrentlyAssigned(client, professional);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewService.existsByClientAndProfessional(clientId, professionalId);
    }

    private boolean isCurrentlyAssigned(User client, User professional) {
        if (professional.getRole() == Role.PERSONAL_TRAINER)
            return professional.equals(client.getAssignedPT());
        if (professional.getRole() == Role.NUTRITIONIST)
            return professional.equals(client.getAssignedNutritionist());
        return false;
    }
}
