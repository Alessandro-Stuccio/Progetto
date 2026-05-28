package com.project.tesi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO per le credenziali di accesso (email + password). Già annotato con @Schema Swagger.
 */
@Schema(description = "Credenziali richieste per effettuare l'accesso e ottenere il token JWT")
public record LoginRequest(
        @Schema(description = "L'indirizzo email dell'utente", example = "pt@test.com")
        @NotBlank(message = "L'email non può essere vuota")
        @Email(message = "Formato email non valido")
        String email,

        @Schema(description = "La password dell'utente", example = "password")
        @NotBlank(message = "La password non può essere vuota")
        String password) {
}
