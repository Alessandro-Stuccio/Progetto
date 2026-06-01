package com.project.tesi.mapper;

import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte le recensioni nel DTO mostrato al pubblico.
 */
@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .authorName(review.getClient().getFirstName())
                .rating(review.getRating())
                .comment(review.getComment())
                .date(review.getCreatedAt())
                .build();
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
