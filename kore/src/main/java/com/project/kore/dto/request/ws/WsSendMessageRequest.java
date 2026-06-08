package com.project.kore.dto.request.ws;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Messaggio WebSocket per inviare un nuovo messaggio in chat (max 2000 caratteri).
 *
 * @param chatId  id della chat di destinazione
 * @param content testo del messaggio (max 2000 caratteri)
 */
public record WsSendMessageRequest(
        @NotNull Long chatId,
        @NotBlank @Size(max = 2000) String content
) {}
