package com.project.tesi.service.impl;

import com.project.tesi.enums.Role;
import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             SlotRepository slotRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    @Transactional
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByClientAndProfessional(Long clientId, Long professionalId) {
        return reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByProfessional(User professional) {
        return reviewRepository.findByProfessional(professional);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating(Long professionalId) {
        Double avg = reviewRepository.getAverageRating(professionalId);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canClientReview(Long clientId, Long professionalId) {
        if (reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId)) {
            return false;
        }
        if (slotRepository.existsByBookedByIdAndProfessionalId(clientId, professionalId)) {
            return true;
        }
        User client = userRepository.findById(clientId).orElse(null);
        User professional = userRepository.findById(professionalId).orElse(null);
        if (client == null || professional == null) return false;
        return isCurrentlyAssigned(client, professional);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId);
    }

    private boolean hasFormerRelationship(User client, User professional) {
        if (slotRepository.existsByBookedByIdAndProfessionalId(client.getId(), professional.getId())) {
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
}
