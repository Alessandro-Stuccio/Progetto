package com.project.tesi.mapper;

import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione di {@link Review} in {@link ReviewResponse}.
 */
@Component
public class ReviewMapper {

    /**
     * Converte una {@link Review} in {@link ReviewResponse}.
     *
     * @param review la recensione da convertire
     * @return il DTO di risposta, o {@code null} se la recensione è {@code null}
     */
    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .authorName(review.getClient().getFirstName())
                .rating(review.getRating())
                .comment(review.getComment())
                .date(review.getCreatedAt())
                .build();
    }

    /**
     * Converte una lista di {@link Review} in una lista di {@link ReviewResponse}.
     *
     * @param reviews lista delle recensioni
     * @return lista dei DTO di risposta
     */
    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
