package com.project.kore.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * Messaggio WebSocket per uscire da una stanza chat.
 *
 * @param roomId id della stanza (chat) da abbandonare
 */
public record LeaveRoomRequest(@NotBlank String roomId) {}
