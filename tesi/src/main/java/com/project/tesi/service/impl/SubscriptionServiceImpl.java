package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementazione di {@link SubscriptionService}.
 * Gestisce gli abbonamenti tramite {@link com.project.tesi.repository.SubscriptionRepository}.
 * {@code findActiveByUserWithLock} usa pessimistic write lock per operazioni
 * concorrenti sui crediti, garantendo isolamento in scenari di prenotazione simultanea.
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
public Subscription getSubscriptionStatus(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user)
                .orElseThrow(SubscriptionNotFoundException::new);
    }

    @Override
    public Subscription save(Subscription sub) {
        return subscriptionRepository.save(sub);
    }

    @Override
    public Optional<Subscription> findActiveByUser(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user);
    }

    @Override
    public Optional<Subscription> findActiveByUserWithLock(User user) {
        return subscriptionRepository.findByUserAndActiveTrueWithLock(user);
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    /**
     * Aggiorna i crediti dell'abbonamento identificato dall'id.
     * Carica l'entità dal repository, sovrascrive i crediti PT e Nutrizionista
     * con i valori forniti e persiste le modifiche.
     *
     * @param subscriptionId id dell'abbonamento da aggiornare
     * @param creditsPT      nuovi crediti per personal trainer
     * @param creditsNutri   nuovi crediti per nutrizionista
     * @return l'abbonamento aggiornato
     * @throws com.project.tesi.exception.common.ResourceNotFoundException se l'abbonamento non esiste
     */
    @Override
    public Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Abbonamento", subscriptionId));

        sub.setCurrentCreditsPT(creditsPT);
        sub.setCurrentCreditsNutri(creditsNutri);
        return subscriptionRepository.save(sub);
    }

    @Override
    public boolean hasSubscribersByPlan(Long planId) {
        return subscriptionRepository.existsByPlanId(planId);
    }
}
