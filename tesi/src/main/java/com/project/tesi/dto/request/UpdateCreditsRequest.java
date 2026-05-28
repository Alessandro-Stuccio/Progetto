package com.project.tesi.dto.request;

import jakarta.validation.constraints.Min;

/**
 * DTO per l'aggiornamento dei crediti PT e nutrizionista di un abbonamento (usato dal moderatore).
 */
public record UpdateCreditsRequest(
        @Min(0) int creditsPT,
        @Min(0) int creditsNutri
) {}
