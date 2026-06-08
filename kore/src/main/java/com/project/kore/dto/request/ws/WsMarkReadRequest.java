package com.project.kore.dto.request.ws;

import jakarta.validation.constraints.NotNull;

/**
 * Messaggio WebSocket per segnalare che i messaggi di una chat sono stati letti.
 *
 * @param chatId id della chat i cui messaggi sono stati letti
 */
public record WsMarkReadRequest(@NotNull Long chatId) {}
