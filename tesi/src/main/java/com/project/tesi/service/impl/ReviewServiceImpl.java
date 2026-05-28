package com.project.tesi.service.impl;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementazione di ReviewService. Gestisce persistenza e query delle recensioni
 * tramite ReviewRepository.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public boolean existsByClientAndProfessional(Long clientId, Long professionalId) {
        return reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId);
    }

    @Override
    public List<Review> findByProfessional(User professional) {
        return reviewRepository.findByProfessional(professional);
    }

    @Override
    public double getAverageRating(Long professionalId) {
        Double avg = reviewRepository.getAverageRating(professionalId);
        return avg != null ? avg : 0.0;
    }

}
