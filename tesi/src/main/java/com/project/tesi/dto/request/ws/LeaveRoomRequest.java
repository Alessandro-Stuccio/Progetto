package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * Messaggio WebSocket per uscire da una stanza chat.
 */
public record LeaveRoomRequest(@NotBlank String roomId) {}
