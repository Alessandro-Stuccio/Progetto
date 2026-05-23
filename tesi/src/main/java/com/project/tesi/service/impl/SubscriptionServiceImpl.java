package com.project.tesi.service.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.strategy.BookingStrategy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final List<BookingStrategy> strategies;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   PlanRepository planRepository,
                                   UserRepository userRepository,
                                   List<BookingStrategy> strategies) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.strategies = strategies;
    }

    @Override
    @Transactional
    public Subscription activateSubscription(PlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Piano", request.planId()));

        // Se esiste già un abbonamento attivo, lo disattiviamo in modo "soft".
        Optional<Subscription> existingActive = subscriptionRepository.findByUserAndActiveTrue(user);
        existingActive.ifPresent(sub -> {
            sub.setActive(false);
            subscriptionRepository.save(sub);
        });

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = plan.getDuration() == PlanDuration.ANNUALE
                ? startDate.plusYears(1)
                : startDate.plusMonths(6);

        Subscription sub = Subscription.builder()
                .user(user)
                .plan(plan)
                .paymentFrequency(request.paymentFrequency())
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .currentCreditsPT(plan.getMonthlyCreditsPT())
                .currentCreditsNutri(plan.getMonthlyCreditsNutri())
                .lastRenewalDate(startDate)
                .build();

        if (request.paymentFrequency() == PaymentFrequency.UNICA_SOLUZIONE) {
            sub.setInstallmentsPaid(1);
            sub.setTotalInstallments(1);
            sub.setNextPaymentDate(null);
        } else {
            sub.setInstallmentsPaid(1);
            sub.setTotalInstallments(plan.getDuration().getMonths());
            sub.setNextPaymentDate(startDate.plusMonths(1));
        }

        return subscriptionRepository.save(sub);
    }

    @Override
    public Subscription getSubscriptionStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        return subscriptionRepository.findByUserAndActiveTrue(user)
                .orElseThrow(SubscriptionNotFoundException::new);
    }

    // Scala i crediti quando scatta l'evento di avvenuta prenotazione (via Observer).
    // Delega il "come" scalare alla Strategy corretta (PT vs Nutri).
    @Override
    @Transactional
    public void deductCredits(Slot slot) {
        User user = slot.getBookedBy();
        User professional = slot.getProfessional();

        try {
            Subscription sub = subscriptionRepository.findByUserAndActiveTrueWithLock(user)
                    .orElseThrow(() -> new IllegalStateException(
                            "Abbonamento non trovato per l'utente " + user.getId()));

            BookingStrategy strategy = strategies.stream()
                    .filter(s -> s.getSupportedRole() == professional.getRole())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Nessuna strategy trovata per il ruolo: " + professional.getRole()));

            strategy.consumeCredits(sub);
            subscriptionRepository.save(sub);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException(
                    "Aggiornamento crediti fallito per conflitto concorrente. Riprovare.", e);
        }
    }

    @Override
    @Transactional
    public void refundCredits(Slot slot) {
        User professional = slot.getProfessional();

        BookingStrategy strategy = strategies.stream()
                .filter(s -> s.getSupportedRole() == professional.getRole())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nessuna strategy trovata per il ruolo: " + professional.getRole()));

        try {
            Subscription sub = subscriptionRepository.findByUserAndActiveTrueWithLock(slot.getBookedBy())
                    .orElseThrow(() -> new IllegalStateException(
                            "Nessun abbonamento attivo trovato per l'utente"));

            strategy.refundCredits(sub);
            subscriptionRepository.save(sub);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException(
                    "Rimborso crediti fallito per conflitto concorrente. Riprovare.", e);
        }
    }
}
