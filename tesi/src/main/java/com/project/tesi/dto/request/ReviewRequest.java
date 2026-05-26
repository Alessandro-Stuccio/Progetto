package com.project.tesi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull Long professionalId,
        @Min(1) @Max(5) int rating,
        @Size(max = 1000, message = "Il commento non può superare 1000 caratteri") String comment) {
}
