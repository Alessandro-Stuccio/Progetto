package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.SubscriptionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubscriptionFacadeImpl implements SubscriptionFacade {

    private final SubscriptionService subscriptionService;

    public SubscriptionFacadeImpl(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    @Transactional(readOnly = true)
    public Subscription getSubscriptionStatus(Long userId) {
        return subscriptionService.getSubscriptionStatus(userId);
    }

    @Override
    @Transactional
    public Subscription activateSubscription(PlanRequest request, Long userId) {
        return subscriptionService.activateSubscription(request, userId);
    }

    @Override
    @Transactional
    public void deductCredits(Slot slot) {
        subscriptionService.deductCredits(slot);
    }

    @Override
    @Transactional
    public void refundCredits(Slot slot) {
        subscriptionService.refundCredits(slot);
    }

    @Override
    @Transactional
    public void refundCreditsIfActive(User client, Role professionalRole) {
        subscriptionService.refundCreditsIfActive(client, professionalRole);
    }

    @Override
    @Transactional
    public void deleteByUser(Long userId) {
        subscriptionService.deleteByUser(userId);
    }
}
