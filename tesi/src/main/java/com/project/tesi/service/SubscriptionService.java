package com.project.tesi.service;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/** Gestione degli abbonamenti degli utenti. */
@Validated
public interface SubscriptionService {

    /** Abbonamento attivo dell'utente. */
    Subscription getSubscriptionStatus(@NotNull User user);

    Subscription save(@NotNull Subscription sub);

    /** Cerca l'abbonamento attivo senza prendere lock. */
    Optional<Subscription> findActiveByUser(@NotNull User user);

    /** Come findActiveByUser, ma con lock pessimistico sulla riga: usato quando si scalano i crediti. */
    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);

    List<Subscription> getAllSubscriptions();

    /** Imposta i crediti PT e nutrizionista dell'abbonamento. */
    Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri);

    /** Dice se qualche abbonamento attivo usa quel piano (serve prima di disabilitarlo). */
    boolean hasSubscribersByPlan(@NotNull @Min(1) Long planId);
}
