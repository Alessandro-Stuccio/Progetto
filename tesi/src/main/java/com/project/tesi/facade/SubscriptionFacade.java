package com.project.tesi.facade;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

/**
 * Facade per l'attivazione di abbonamenti.
 */
public interface SubscriptionFacade {

    /**
     * Attiva un abbonamento per l'utente con il piano e la frequenza di pagamento specificati.
     *
     * @param user             utente per cui attivare l'abbonamento
     * @param plan             piano di abbonamento da attivare
     * @param paymentFrequency frequenza di pagamento scelta (mensile, semestrale, annuale, ecc.)
     * @return abbonamento attivato
     */
    Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency);
}
