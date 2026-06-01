package com.project.tesi.facade;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

/**
 * Attivazione degli abbonamenti.
 */
public interface SubscriptionFacade {

    /**
     * Attiva per l'utente un abbonamento al piano indicato con la frequenza di pagamento scelta.
     */
    Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency);
}
