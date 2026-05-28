package com.project.tesi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO per l'invio di un messaggio in una chat via REST.
 */
public record SendMessageRequest(
        @NotNull(message = "L'ID della chat è obbligatorio") Long chatId,
        @NotBlank(message = "Il contenuto del messaggio non può essere vuoto")
        @Size(max = 2000, message = "Il messaggio non può superare 2000 caratteri") String content) {
}
