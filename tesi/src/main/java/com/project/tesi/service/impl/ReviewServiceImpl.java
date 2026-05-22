package com.project.tesi.service.impl;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.review.ReviewNotAllowedException;
import com.project.tesi.mapper.ReviewMapper;
import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio per la gestione delle recensioni.
 *
 * Regole di business applicate:
 * <ul>
 *   <li>Unicità: una sola recensione per coppia cliente-professionista</li>
 *   <li>Relazione formale: il cliente può recensire solo se ha avuto almeno una
 *       prenotazione con il professionista, oppure è attualmente assegnato a lui</li>
 *   <li>Voto: da 1 a 5 stelle con commento opzionale</li>
 * </ul>
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             BookingRepository bookingRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        User professional = userRepository.findById(request.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", request.professionalId()));

        // REGOLA BUSINESS: Unicità — una sola recensione per coppia cliente-professionista
        if (reviewRepository.existsByClientIdAndProfessionalId(user.getId(), professional.getId())) {
            throw new ResourceAlreadyExistsException("Hai già lasciato una recensione per questo professionista.");
        }

        // REGOLA BUSINESS: Relazione formale — almeno una prenotazione storica
        // oppure assegnazione corrente attiva
        if (!hasFormerRelationship(user, professional)) {
            throw new ReviewNotAllowedException(
                    "Puoi recensire solo professionisti con cui hai avuto un rapporto formale.");
        }

        Review review = Review.builder()
                .client(user)
                .professional(professional)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        return reviewRepository.findByProfessional(professional).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canClientReview(Long clientId, Long professionalId) {
        if (reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId)) {
            return false;
        }
        if (bookingRepository.existsByUserIdAndProfessionalId(clientId, professionalId)) {
            return true;
        }
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", clientId));
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));
        return isCurrentlyAssigned(client, professional);
    }

    private boolean hasFormerRelationship(User client, User professional) {
        if (bookingRepository.existsByUserIdAndProfessionalId(client.getId(), professional.getId())) {
            return true;
        }
        return isCurrentlyAssigned(client, professional);
    }

    private boolean isCurrentlyAssigned(User client, User professional) {
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            return professional.equals(client.getAssignedPT());
        }
        if (professional.getRole() == Role.NUTRITIONIST) {
            return professional.equals(client.getAssignedNutritionist());
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId);
    }

}