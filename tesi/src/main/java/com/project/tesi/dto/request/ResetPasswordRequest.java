package com.project.tesi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Imposta la nuova password usando il token ricevuto via email.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Il token è obbligatorio")
        String token,

        @NotBlank(message = "La nuova password è obbligatoria")
        @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
        String newPassword) {
}
