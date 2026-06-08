package com.project.kore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Recensione lasciata a un professionista: voto da 1 a 5 e commento facoltativo (max 1000 caratteri).
 *
 * @param professionalId id del professionista recensito
 * @param rating         voto da 1 a 5
 * @param comment        commento testuale (opzionale, max 1000 caratteri)
 */
public record ReviewRequest(
        @NotNull Long professionalId,
        @Min(1) @Max(5) int rating,
        @Size(max = 1000, message = "Il commento non può superare 1000 caratteri") String comment) {
}
