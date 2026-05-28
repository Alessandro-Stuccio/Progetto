package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO WebSocket per l'invio di un nuovo messaggio in una stanza chat.
 */
public record WsSendMessageRequest(
        @NotNull Long chatId,
        @NotBlank @Size(max = 2000) String content
) {}
