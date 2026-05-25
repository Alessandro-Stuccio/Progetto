package com.project.tesi.service;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public interface SubscriptionService {

    Subscription getSubscriptionStatus(@NotNull Long userId);

    Subscription save(@NotNull Subscription sub);

    Optional<Subscription> findActiveByUser(@NotNull User user);

    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);
}
