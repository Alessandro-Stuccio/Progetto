package com.project.tesi.dto.request;

import com.project.tesi.enums.PaymentFrequency;
import jakarta.validation.constraints.NotNull;

/**
 * Sceglie un piano e con quale frequenza pagarlo.
 */
public record PlanRequest(
        @NotNull Long planId,
        @NotNull PaymentFrequency paymentFrequency) {
}
