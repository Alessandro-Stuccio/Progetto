package com.project.tesi.service;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public interface SubscriptionService {

    Subscription getSubscriptionStatus(@NotNull User user);

    Subscription getSubscriptionStatus(@NotNull @Min(1) Long userId);

    Subscription activateSubscription(@NotNull PlanRequest request, @NotNull @Min(1) Long userId);

    void deductCredits(@NotNull Slot slot);

    void refundCredits(@NotNull Slot slot);

    void refundCreditsIfActive(@NotNull User client, @NotNull Role professionalRole);

    Subscription save(@NotNull Subscription sub);

    Optional<Subscription> findActiveByUser(@NotNull User user);

    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);

    void deleteByUser(@NotNull @Min(1) Long userId);
}
