package com.project.tesi.service;

import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione delle recensioni lasciate ai professionisti. */
@Validated
public interface ReviewService {

    Review save(@NotNull Review review);

    /** Dice se quel cliente ha già recensito quel professionista (ne è ammessa una sola). */
    boolean existsByClientAndProfessional(@NotNull Long clientId, @NotNull Long professionalId);

    List<Review> findByProfessional(@NotNull User professional);

    /** Media dei voti ricevuti dal professionista. */
    double getAverageRating(@NotNull Long professionalId);
}
