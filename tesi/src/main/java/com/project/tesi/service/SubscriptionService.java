package com.project.tesi.service;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
public interface SubscriptionService {

    Subscription getSubscriptionStatus(@NotNull User user);

    Subscription save(@NotNull Subscription sub);

    Optional<Subscription> findActiveByUser(@NotNull User user);

    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);

    List<Subscription> getAllSubscriptions();

    Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri);

    boolean hasSubscribersByPlan(@NotNull @Min(1) Long planId);
}
