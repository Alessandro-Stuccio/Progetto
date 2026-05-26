package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

public interface SubscriptionFacade {

    Subscription activateSubscription(PlanRequest request, Long userId);

    Subscription getSubscriptionStatus(Long userId);

    void deductCredits(Slot slot);

    void refundCredits(Slot slot);

    void refundCreditsIfActive(User client, Role professionalRole);

    void deleteByUser(Long userId);
}
