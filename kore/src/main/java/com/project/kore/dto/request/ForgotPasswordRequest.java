package com.project.kore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Avvia il recupero password: serve solo l'email a cui inviare il link.
 *
 * @param email email dell'account per cui avviare il reset
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Formato email non valido")
        String email) {
}
