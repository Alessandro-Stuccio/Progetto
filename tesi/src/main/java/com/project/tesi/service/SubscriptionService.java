package com.project.tesi.service;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SubscriptionService {

    Subscription activateSubscription(@NotNull PlanRequest request, @NotNull Long userId);

    Subscription getSubscriptionStatus(@NotNull Long userId);

    void deductCredits(@NotNull Slot slot);

    void refundCredits(@NotNull Slot slot);
}
