package com.project.kore.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Aggiorna i crediti PT, nutrizionista e psicologo di un abbonamento (azione del moderatore).
 *
 * @param creditsPT    nuovi crediti per il personal trainer
 * @param creditsNutri nuovi crediti per il nutrizionista
 * @param creditsPsico nuovi crediti per lo psicologo
 */
public record UpdateCreditsRequest(
        @Min(0) int creditsPT,
        @Min(0) int creditsNutri,
        @Min(0) int creditsPsico
) {}
