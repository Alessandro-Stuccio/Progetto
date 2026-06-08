package com.project.kore.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Prenota uno slot a partire dal suo id.
 */
public record BookingRequest(@NotNull Long slotId) {
}
