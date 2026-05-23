package com.project.tesi.service;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ReviewService {

    Review save(@NotNull Review review);

    boolean existsByClientAndProfessional(@NotNull Long clientId, @NotNull Long professionalId);

    List<Review> findByProfessional(@NotNull User professional);

    double getAverageRating(@NotNull Long professionalId);

    boolean canClientReview(@NotNull Long clientId, @NotNull Long professionalId);

    boolean hasClientReviewed(@NotNull Long clientId, @NotNull Long professionalId);
}
