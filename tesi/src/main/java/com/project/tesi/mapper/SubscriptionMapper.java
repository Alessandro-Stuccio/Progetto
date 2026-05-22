package com.project.tesi.mapper;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionMapper {

    public Subscription toSubscription(RegisterRequest request, User user, Plan plan) {
        if (request == null || user == null || plan == null) return null;
        return buildSubscription(user, plan, request.paymentFrequency());
    }

    public Subscription toSubscriptionFromAdmin(User user, Plan plan, PaymentFrequency paymentFrequency) {
        return buildSubscription(user, plan, paymentFrequency);
    }

    public SubscriptionResponse toResponse(Subscription s) {
        if (s == null) return null;
        Plan plan = s.getPlan();
        Double monthlyPrice = plan != null ? plan.getMonthlyInstallmentPrice() : null;
        return SubscriptionResponse.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .userName(s.getUser() != null ? s.getUser().getFullName() : null)
                .planName(plan != null ? plan.getName() : null)
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .active(s.isActive())
                .currentCreditsPT(s.getCurrentCreditsPT())
                .currentCreditsNutri(s.getCurrentCreditsNutri())
                .monthlyPrice(monthlyPrice)
                .build();
    }

    public List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Subscription buildSubscription(User user, Plan plan, PaymentFrequency paymentFrequency) {
        LocalDate startDate = LocalDate.now();
        int months = plan.getDuration().getMonths();
        LocalDate endDate = startDate.plusMonths(months);
        int totalInstallments = paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? 1 : months;

        return Subscription.builder()
                .user(user)
                .plan(plan)
                .paymentFrequency(paymentFrequency)
                .installmentsPaid(1)
                .totalInstallments(totalInstallments)
                .nextPaymentDate(paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? null : startDate.plusMonths(1))
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .currentCreditsPT(plan.getMonthlyCreditsPT())
                .currentCreditsNutri(plan.getMonthlyCreditsNutri())
                .lastRenewalDate(startDate)
                .build();
    }
}
