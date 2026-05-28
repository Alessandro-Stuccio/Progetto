package com.project.tesi.service.strategy;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.InsufficientCreditsException;
import com.project.tesi.exception.booking.ProfessionalNotAssignedException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

/**
 * Strategia di prenotazione specifica per il Personal Trainer.
 * Verifica che il cliente abbia un PT assegnato corrispondente
 * e che abbia crediti PT residui nell'abbonamento.
 */
@Component
public class PersonalTrainerBookingStrategy implements BookingStrategy {

    /** {@inheritDoc} */
    @Override
    public Role getSupportedRole() {
        return Role.PERSONAL_TRAINER;
    }

    /**
     * Verifica che il PT assegnato al cliente corrisponda al professionista richiesto.
     *
     * @throws ProfessionalNotAssignedException se il cliente non è assegnato a quel PT
     */
    @Override
    public void verifyAssignment(User client, User professional) {
        if (client.getAssignedPT() == null || !client.getAssignedPT().getId().equals(professional.getId())) {
            throw new ProfessionalNotAssignedException("Personal Trainer");
        }
    }

    /**
     * Verifica che i crediti PT siano sufficienti e ne scala uno.
     *
     * @throws InsufficientCreditsException se i crediti PT sono esauriti
     */
    @Override
    public void consumeCredits(Subscription subscription) {
        if (subscription.getCurrentCreditsPT() <= 0) {
            throw new InsufficientCreditsException("PT");
        }
        subscription.setCurrentCreditsPT(subscription.getCurrentCreditsPT() - 1);
    }

    /** {@inheritDoc} */
    @Override
    public void refundCredits(Subscription subscription) {
        subscription.setCurrentCreditsPT(subscription.getCurrentCreditsPT() + 1);
    }
}
