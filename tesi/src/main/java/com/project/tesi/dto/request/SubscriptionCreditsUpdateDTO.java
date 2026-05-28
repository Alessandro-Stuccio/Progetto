package com.project.tesi.dto.request;

import jakarta.validation.constraints.Min;

/**
 * DTO per l'aggiornamento manuale dei crediti di un abbonamento (usato dall'admin).
 */
public record SubscriptionCreditsUpdateDTO(
        @Min(value = 0, message = "I crediti PT non possono essere negativi") Integer creditsPT,
        @Min(value = 0, message = "I crediti nutrizionista non possono essere negativi") Integer creditsNutri
) {}
