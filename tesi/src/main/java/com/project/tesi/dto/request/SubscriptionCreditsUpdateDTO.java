package com.project.tesi.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Ritocca a mano i crediti PT e nutrizionista di un abbonamento (azione admin).
 *
 * @param creditsPT    nuovi crediti per il personal trainer
 * @param creditsNutri nuovi crediti per il nutrizionista
 */
public record SubscriptionCreditsUpdateDTO(
        @Min(value = 0, message = "I crediti PT non possono essere negativi") Integer creditsPT,
        @Min(value = 0, message = "I crediti nutrizionista non possono essere negativi") Integer creditsNutri
) {}
