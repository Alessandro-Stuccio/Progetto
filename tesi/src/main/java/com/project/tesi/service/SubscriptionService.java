package com.project.tesi.service;

import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Servizio per la gestione degli abbonamenti degli utenti.
 */
@Validated
public interface SubscriptionService {

    /**
     * Recupera lo stato dell'abbonamento attivo di un utente.
     *
     * @param user utente di cui verificare l'abbonamento
     * @return abbonamento attivo dell'utente
     */
    Subscription getSubscriptionStatus(@NotNull User user);

    /**
     * Persiste un abbonamento nel database.
     *
     * @param sub entità abbonamento da salvare
     * @return abbonamento salvato
     */
    Subscription save(@NotNull Subscription sub);

    /**
     * Cerca l'abbonamento attivo di un utente senza lock.
     *
     * @param user utente da ricercare
     * @return {@link Optional} contenente l'abbonamento attivo, vuoto se assente
     */
    Optional<Subscription> findActiveByUser(@NotNull User user);

    /**
     * Cerca l'abbonamento attivo di un utente acquisendo un lock pessimistico sulla riga.
     *
     * @param user utente da ricercare
     * @return {@link Optional} contenente l'abbonamento attivo con lock, vuoto se assente
     */
    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);

    /**
     * Restituisce tutti gli abbonamenti presenti nel sistema.
     *
     * @return lista di tutti gli abbonamenti
     */
    List<Subscription> getAllSubscriptions();

    /**
     * Aggiorna i crediti di un abbonamento per personal trainer e nutrizionista.
     *
     * @param subscriptionId identificativo dell'abbonamento
     * @param creditsPT      nuovi crediti per personal trainer
     * @param creditsNutri   nuovi crediti per nutrizionista
     * @return abbonamento aggiornato
     */
    Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri);

    /**
     * Verifica se esistono abbonamenti attivi associati a un piano specifico.
     *
     * @param planId identificativo del piano
     * @return {@code true} se almeno un abbonato usa quel piano, {@code false} altrimenti
     */
    boolean hasSubscribersByPlan(@NotNull @Min(1) Long planId);
}
