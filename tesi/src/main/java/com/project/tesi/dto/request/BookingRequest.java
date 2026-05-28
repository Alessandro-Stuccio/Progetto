package com.project.tesi.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO per la richiesta di prenotazione di uno slot.
 */
public record BookingRequest(@NotNull Long slotId) {
}
