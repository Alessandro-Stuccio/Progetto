package com.project.tesi.dto.request.ws;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO WebSocket per la notifica di uscita da una stanza chat.
 */
public record LeaveRoomRequest(@NotBlank String roomId) {}
