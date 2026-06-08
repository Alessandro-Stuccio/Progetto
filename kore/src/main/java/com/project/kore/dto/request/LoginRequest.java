package com.project.kore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenziali di accesso: email e password.
 *
 * @param email    email dell'account (fa anche da username)
 * @param password password in chiaro
 */
public record LoginRequest(
        @NotBlank(message = "L'email non può essere vuota")
        @Email(message = "Formato email non valido")
        String email,

        @NotBlank(message = "La password non può essere vuota")
        String password) {
}
