package com.project.kore.service.strategy;

import com.project.kore.enums.Role;
import com.project.kore.exception.booking.InsufficientCreditsException;
import com.project.kore.exception.booking.ProfessionalNotAssignedException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PsychologistBookingStrategyTest {

    private PsychologistBookingStrategy strategy;

    private User client;
    private User professional;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        strategy = new PsychologistBookingStrategy();

        professional = new User();
        professional.setId(1L);

        client = new User();
        client.setId(2L);
        client.setAssignedPsychologist(professional);

        subscription = new Subscription();
        subscription.setCurrentCreditsPsico(2);
    }

    @Test
    @DisplayName("getSupportedRole — restituisce PSYCHOLOGIST")
    void getSupportedRole_returnsPsychologist() {
        assertThat(strategy.getSupportedRole()).isEqualTo(Role.PSYCHOLOGIST);
    }

    @Test
    @DisplayName("verifyAssignment — psicologo assegnato coincide: nessuna eccezione")
    void verifyAssignment_psicoAssigned_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> strategy.verifyAssignment(client, professional));
    }

    @Test
    @DisplayName("verifyAssignment — psicologo null: lancia ProfessionalNotAssignedException")
    void verifyAssignment_psicoNull_throwsProfessionalNotAssignedException() {
        client.setAssignedPsychologist(null);
        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("verifyAssignment — psicologo diverso da quello richiesto: lancia ProfessionalNotAssignedException")
    void verifyAssignment_differentPsico_throwsProfessionalNotAssignedException() {
        User otherPsico = new User();
        otherPsico.setId(99L);
        client.setAssignedPsychologist(otherPsico);

        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("consumeCredits — crediti sufficienti: decrementa di 1")
    void consumeCredits_sufficientCredits_decrementsCredits() {
        strategy.consumeCredits(subscription);
        assertThat(subscription.getCurrentCreditsPsico()).isEqualTo(1);
    }

    @Test
    @DisplayName("consumeCredits — crediti a 0: lancia InsufficientCreditsException")
    void consumeCredits_zeroCredits_throwsInsufficientCreditsException() {
        subscription.setCurrentCreditsPsico(0);
        assertThatThrownBy(() -> strategy.consumeCredits(subscription))
                .isInstanceOf(InsufficientCreditsException.class);
    }

    @Test
    @DisplayName("refundCredits — incrementa currentCreditsPsico di 1")
    void refundCredits_incrementsCurrentCreditsPsico() {
        subscription.setCurrentCreditsPsico(1);
        strategy.refundCredits(subscription);
        assertThat(subscription.getCurrentCreditsPsico()).isEqualTo(2);
    }
}
