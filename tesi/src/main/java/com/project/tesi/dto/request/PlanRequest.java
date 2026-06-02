package com.project.tesi.dto.request;

import com.project.tesi.enums.PaymentFrequency;
import jakarta.validation.constraints.NotNull;

/**
 * Sceglie un piano e con quale frequenza pagarlo.
 *
 * @param planId           id del piano scelto
 * @param paymentFrequency frequenza di pagamento scelta
 */
public record PlanRequest(
        @NotNull Long planId,
        @NotNull PaymentFrequency paymentFrequency) {
}
