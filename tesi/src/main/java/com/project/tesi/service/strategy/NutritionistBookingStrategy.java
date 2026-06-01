package com.project.tesi.service.strategy;

import org.springframework.stereotype.Component;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.InsufficientCreditsException;
import com.project.tesi.exception.booking.ProfessionalNotAssignedException;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;

/** Regole di prenotazione per il nutrizionista: lavora sul nutrizionista assegnato e sui crediti nutri. */
@Component
public class NutritionistBookingStrategy implements BookingStrategy {

    @Override
    public Role getSupportedRole() {
        return Role.NUTRITIONIST;
    }

    @Override
    public void verifyAssignment(User client, User professional) {
        if (client.getAssignedNutritionist() == null
                || !client.getAssignedNutritionist().getId().equals(professional.getId())) {
            throw new ProfessionalNotAssignedException("Nutrizionista");
        }
    }

    @Override
    public void consumeCredits(Subscription subscription) {
        if (subscription.getCurrentCreditsNutri() <= 0) {
            throw new InsufficientCreditsException("Nutrizionista");
        }
        subscription.setCurrentCreditsNutri(subscription.getCurrentCreditsNutri() - 1);
    }

    @Override
    public void refundCredits(Subscription subscription) {
        subscription.setCurrentCreditsNutri(subscription.getCurrentCreditsNutri() + 1);
    }
}
