package com.project.kore.facade;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;

/**
 * Attivazione degli abbonamenti.
 */
public interface SubscriptionFacade {

    /**
     * Attiva per l'utente un abbonamento al piano indicato con la frequenza di pagamento scelta.
     *
     * @param user             l'utente che sottoscrive
     * @param plan             il piano scelto
     * @param paymentFrequency la frequenza di pagamento (es. unica soluzione o rate)
     * @return l'abbonamento attivato
     */
    Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency);
}
