package com.project.kore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Candidatura per una posizione lavorativa: dati anagrafici, ruolo desiderato e messaggio motivazionale (max 2000 caratteri).
 *
 * @param firstName nome del candidato
 * @param lastName  cognome del candidato
 * @param email     email di contatto
 * @param role      ruolo per cui ci si candida
 * @param message   messaggio motivazionale (max 2000 caratteri)
 */
public record JobApplicationRequest(
        @NotBlank(message = "Il nome è obbligatorio") String firstName,
        @NotBlank(message = "Il cognome è obbligatorio") String lastName,
        @NotBlank(message = "L'email è obbligatoria") @Email(message = "Formato email non valido") String email,
        @NotBlank(message = "Il ruolo è obbligatorio") String role,
        @NotBlank(message = "Il messaggio motivazionale è obbligatorio")
        @Size(max = 2000, message = "Il messaggio non può superare 2000 caratteri") String message) {
}
