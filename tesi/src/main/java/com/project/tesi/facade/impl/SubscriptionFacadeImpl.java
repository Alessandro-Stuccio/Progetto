package com.project.tesi.facade.impl;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.SubscriptionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class SubscriptionFacadeImpl implements SubscriptionFacade {

    private final SubscriptionService subscriptionService;

    public SubscriptionFacadeImpl(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

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
