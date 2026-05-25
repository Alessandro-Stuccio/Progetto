package com.project.tesi.service.impl;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final SlotRepository slotRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, SlotRepository slotRepository) {
        this.reviewRepository = reviewRepository;
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
    public boolean hasBookingRelationship(Long clientId, Long professionalId) {
        return slotRepository.existsByBookedByIdAndProfessionalId(clientId, professionalId);
    }

}
