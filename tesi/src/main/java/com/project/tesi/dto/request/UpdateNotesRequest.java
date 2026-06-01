package com.project.tesi.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Aggiorna le note testuali di un documento (max 1000 caratteri).
 */
public record UpdateNotesRequest(@Size(max = 1000) String notes) {}
