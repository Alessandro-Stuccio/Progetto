package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO WebSocket per la richiesta di ingresso in una stanza chat.
 */
public record JoinRoomRequest(@NotBlank String roomId) {}
