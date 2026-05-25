package com.project.tesi.facade;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewFacade {
    ReviewResponse addReview(ReviewRequest request, Long userId);
    List<ReviewResponse> getReviewsForProfessional(Long professionalId);
    boolean canClientReview(Long clientId, Long professionalId);
    boolean hasClientReviewed(Long clientId, Long professionalId);
}
