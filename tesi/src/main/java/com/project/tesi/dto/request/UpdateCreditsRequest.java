package com.project.tesi.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Aggiorna i crediti PT e nutrizionista di un abbonamento (azione del moderatore).
 */
public record UpdateCreditsRequest(
        @Min(0) int creditsPT,
        @Min(0) int creditsNutri
) {}
