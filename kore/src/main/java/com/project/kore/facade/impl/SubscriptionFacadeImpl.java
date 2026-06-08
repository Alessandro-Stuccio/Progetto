package com.project.kore.facade.impl;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.PlanDuration;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.SubscriptionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Attiva gli abbonamenti calcolando date, crediti e rate in base al piano e alla frequenza scelti.
 */
@Component
public class SubscriptionFacadeImpl implements SubscriptionFacade {

    private final SubscriptionService subscriptionService;

    public SubscriptionFacadeImpl(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Disattiva l'eventuale abbonamento attivo e ne crea uno nuovo: la scadenza dipende dalla
     * durata del piano (annuale o semestrale) e i crediti iniziali vengono presi dal piano.
     * In soluzione unica non c'è prossima scadenza di pagamento; a rate, le rate sono pari ai mesi
     * di durata e la prima scadenza è fra un mese.
     */
    @Override
    @Transactional
    public Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency) {
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
                .paymentFrequency(paymentFrequency)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .currentCreditsPT(plan.getMonthlyCreditsPT())
                .currentCreditsNutri(plan.getMonthlyCreditsNutri())
                .lastRenewalDate(startDate)
                .build();

        if (paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE) {
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

}
