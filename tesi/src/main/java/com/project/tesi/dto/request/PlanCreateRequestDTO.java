package com.project.tesi.dto.request;

import jakarta.validation.constraints.*;

/**
 * Crea o aggiorna un piano di abbonamento: nome, durata, prezzi (pieno e a rata mensile) e crediti mensili per PT e nutrizionista.
 */
public record PlanCreateRequestDTO(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank String duration,
        @NotNull @Positive Double fullPrice,
        @NotNull @Positive Double monthlyInstallmentPrice,
        @Min(0) Integer monthlyCreditsPT,
        @Min(0) Integer monthlyCreditsNutri
) {}
