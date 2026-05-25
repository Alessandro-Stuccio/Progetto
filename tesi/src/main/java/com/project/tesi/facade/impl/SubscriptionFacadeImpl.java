package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.strategy.BookingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionFacadeImpl implements SubscriptionFacade {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionFacadeImpl.class);

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final PlanService planService;
    private final List<BookingStrategy> strategies;

    public SubscriptionFacadeImpl(SubscriptionService subscriptionService,
                                   UserService userService,
                                   PlanService planService,
                                   List<BookingStrategy> strategies) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.planService = planService;
        this.strategies = strategies;
    }

    @Override
    @Transactional
    public Subscription activateSubscription(PlanRequest request, Long userId) {
        User user = userService.getUserById(userId);
        Plan plan = planService.getPlanById(request.planId());

        subscriptionService.findActiveByUser(user).ifPresent(existing -> {
            existing.setActive(false);
            subscriptionService.save(existing);
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

        return subscriptionService.save(sub);
    }

    @Override
    @Transactional
    public void deductCredits(Slot slot) {
        User user = slot.getBookedBy();
        User professional = slot.getProfessional();

        try {
            Subscription sub = subscriptionService.findActiveByUserWithLock(user)
                    .orElseThrow(() -> new IllegalStateException(
                            "Abbonamento non trovato per l'utente " + user.getId()));

            BookingStrategy strategy = resolveStrategy(professional.getRole());
            strategy.consumeCredits(sub);
            subscriptionService.save(sub);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException(
                    "Aggiornamento crediti fallito per conflitto concorrente. Riprovare.", e);
        }
    }

    @Override
    @Transactional
    public void refundCredits(Slot slot) {
        User professional = slot.getProfessional();
        BookingStrategy strategy = resolveStrategy(professional.getRole());

        try {
            Subscription sub = subscriptionService.findActiveByUserWithLock(slot.getBookedBy())
                    .orElseThrow(() -> new IllegalStateException(
                            "Nessun abbonamento attivo trovato per l'utente"));

            strategy.refundCredits(sub);
            subscriptionService.save(sub);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException(
                    "Rimborso crediti fallito per conflitto concorrente. Riprovare.", e);
        }
    }

    @Override
    @Transactional
    public void refundCreditsIfActive(User client, Role professionalRole) {
        BookingStrategy strategy = resolveStrategy(professionalRole);

        Optional<Subscription> subOpt = subscriptionService.findActiveByUserWithLock(client);
        if (subOpt.isEmpty()) {
            log.warn("Nessun abbonamento attivo per utente ID {}: rimborso non effettuato.", client.getId());
            return;
        }

        strategy.refundCredits(subOpt.get());
        subscriptionService.save(subOpt.get());
    }

    private BookingStrategy resolveStrategy(Role role) {
        return strategies.stream()
                .filter(s -> s.getSupportedRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nessuna strategy trovata per il ruolo: " + role));
    }
}
