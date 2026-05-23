package com.project.tesi.dto.request;

import jakarta.validation.constraints.*;

public record UserCreateRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String role,
        @Min(1) Long assignedPTId,
        @Min(1) Long assignedNutritionistId,
        @Min(1) Long planId,
        String paymentFrequency
) {}
