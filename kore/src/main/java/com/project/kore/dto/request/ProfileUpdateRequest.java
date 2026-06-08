package com.project.kore.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Aggiorna il proprio profilo. Ogni campo è opzionale: si modifica solo ciò che viene valorizzato.
 *
 * @param firstName      nuovo nome (opzionale)
 * @param lastName       nuovo cognome (opzionale)
 * @param password       nuova password (opzionale)
 * @param profilePicture nuovo URL immagine di profilo (opzionale)
 */
public record ProfileUpdateRequest(
        @Size(min = 1, max = 100, message = "Il nome deve essere tra 1 e 100 caratteri")
        String firstName,
        @Size(min = 1, max = 100, message = "Il cognome deve essere tra 1 e 100 caratteri")
        String lastName,
        @Size(min = 6, max = 100, message = "La password deve avere almeno 6 caratteri")
        String password,
        @Size(max = 500, message = "URL immagine troppo lungo")
        String profilePicture) {
}
