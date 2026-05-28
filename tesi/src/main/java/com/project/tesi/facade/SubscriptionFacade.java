package com.project.tesi.facade;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

public interface SubscriptionFacade {

    Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency);
}
