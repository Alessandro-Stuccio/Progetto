package com.project.kore.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * Messaggio WebSocket per entrare in una stanza chat.
 *
 * @param roomId id della stanza (chat) a cui unirsi
 */
public record JoinRoomRequest(@NotBlank String roomId) {}
