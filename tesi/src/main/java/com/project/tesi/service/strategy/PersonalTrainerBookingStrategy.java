package com.project.tesi.service.strategy;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.InsufficientCreditsException;
import com.project.tesi.exception.booking.ProfessionalNotAssignedException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

/** Regole di prenotazione per il personal trainer: lavora sul PT assegnato e sui crediti PT. */
@Component
public class PersonalTrainerBookingStrategy implements BookingStrategy {

    @Override
    public Role getSupportedRole() {
        return Role.PERSONAL_TRAINER;
    }

    @Override
    public void verifyAssignment(User client, User professional) {
        if (client.getAssignedPT() == null || !client.getAssignedPT().getId().equals(professional.getId())) {
            throw new ProfessionalNotAssignedException("Personal Trainer");
        }
    }

    @Override
    public void consumeCredits(Subscription subscription) {
        if (subscription.getCurrentCreditsPT() <= 0) {
            throw new InsufficientCreditsException("PT");
        }
        subscription.setCurrentCreditsPT(subscription.getCurrentCreditsPT() - 1);
    }

    @Override
    public void refundCredits(Subscription subscription) {
        subscription.setCurrentCreditsPT(subscription.getCurrentCreditsPT() + 1);
    }
}
