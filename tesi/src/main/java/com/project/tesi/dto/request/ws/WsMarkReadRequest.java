package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotNull;

/**
 * DTO WebSocket per segnalare che i messaggi di una chat sono stati letti o consegnati.
 */
public record WsMarkReadRequest(@NotNull Long chatId) {}
