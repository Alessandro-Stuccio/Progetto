package com.project.kore.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Aggiorna le note testuali di un documento (max 1000 caratteri).
 *
 * @param notes nuovo testo delle note (max 1000 caratteri)
 */
public record UpdateNotesRequest(@Size(max = 1000) String notes) {}
