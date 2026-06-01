package com.project.tesi.dto.request;

import com.project.tesi.enums.PaymentFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registrazione di un nuovo cliente: dati anagrafici, credenziali e scelta di PT e nutrizionista (obbligatori).
 * Piano e frequenza di pagamento sono facoltativi e possono essere definiti in seguito.
 */
public record RegisterRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare 100 caratteri")
        String firstName,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 100, message = "Il cognome non può superare 100 caratteri")
        String lastName,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        String email,

        @NotBlank
        @Size(min = 6, max = 100, message = "La password deve avere tra 6 e 100 caratteri")
        String password,

        @NotNull(message = "È necessario selezionare un Personal Trainer")
        Long selectedPtId,

        @NotNull(message = "È necessario selezionare un Nutrizionista")
        Long selectedNutritionistId,

        @Size(max = 500, message = "URL immagine troppo lungo")
        String profilePicture,

        Long selectedPlanId,

        PaymentFrequency paymentFrequency) {
}
