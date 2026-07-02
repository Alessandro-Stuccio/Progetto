package com.project.kore.service.strategy;

import org.springframework.stereotype.Component;
import com.project.kore.enums.Role;
import com.project.kore.exception.booking.InsufficientCreditsException;
import com.project.kore.exception.booking.ProfessionalNotAssignedException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;

/** Regole di prenotazione per lo psicologo: lavora sullo psicologo assegnato e sui crediti psico. */
@Component
public class PsychologistBookingStrategy implements BookingStrategy {

    @Override
    public Role getSupportedRole() {
        return Role.PSYCHOLOGIST;
    }

    @Override
    public void verifyAssignment(User client, User professional) {
        if (client.getAssignedPsychologist() == null
                || !client.getAssignedPsychologist().getId().equals(professional.getId())) {
            throw new ProfessionalNotAssignedException("Psicologo");
        }
    }

    @Override
    public void consumeCredits(Subscription subscription) {
        if (subscription.getCurrentCreditsPsico() <= 0) {
            throw new InsufficientCreditsException("Psicologo");
        }
        subscription.setCurrentCreditsPsico(subscription.getCurrentCreditsPsico() - 1);
    }

    @Override
    public void refundCredits(Subscription subscription) {
        subscription.setCurrentCreditsPsico(subscription.getCurrentCreditsPsico() + 1);
    }
}
