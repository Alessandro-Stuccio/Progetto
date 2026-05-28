package com.project.tesi.dto.request;

import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento delle note testuali di un documento.
 */
public record UpdateNotesRequest(@Size(max = 1000) String notes) {}
