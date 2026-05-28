package com.project.tesi.dto.request;

import com.project.tesi.enums.PaymentFrequency;
import jakarta.validation.constraints.NotNull;

/**
 * DTO per la selezione di un piano di abbonamento e la relativa frequenza di pagamento.
 */
public record PlanRequest(
        @NotNull Long planId,
        @NotNull PaymentFrequency paymentFrequency) {
}
