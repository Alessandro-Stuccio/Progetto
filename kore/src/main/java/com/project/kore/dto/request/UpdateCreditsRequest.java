package com.project.kore.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Aggiorna i crediti PT e nutrizionista di un abbonamento (azione del moderatore).
 *
 * @param creditsPT    nuovi crediti per il personal trainer
 * @param creditsNutri nuovi crediti per il nutrizionista
 */
public record UpdateCreditsRequest(
        @Min(0) int creditsPT,
        @Min(0) int creditsNutri
) {}
