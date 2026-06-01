package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * Messaggio WebSocket per entrare in una stanza chat.
 */
public record JoinRoomRequest(@NotBlank String roomId) {}
