package com.project.tesi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento parziale di un utente da parte del moderatore. Tutti i campi sono opzionali.
 */
public record ModeratorUserUpdateRequest(

        @Email(message = "Il formato dell'email non è valido")
        String email,

        @Size(min = 1, max = 100, message = "Il nome deve essere tra 1 e 100 caratteri")
        String firstName,

        @Size(min = 1, max = 100, message = "Il cognome deve essere tra 1 e 100 caratteri")
        String lastName,

        @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
        String password
) {}
