package com.project.kore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Invia un messaggio in una chat via REST (max 2000 caratteri).
 *
 * @param chatId  id della chat di destinazione
 * @param content testo del messaggio (max 2000 caratteri)
 */
public record SendMessageRequest(
        @NotNull(message = "L'ID della chat è obbligatorio") Long chatId,
        @NotBlank(message = "Il contenuto del messaggio non può essere vuoto")
        @Size(max = 2000, message = "Il messaggio non può superare 2000 caratteri") String content) {
}
