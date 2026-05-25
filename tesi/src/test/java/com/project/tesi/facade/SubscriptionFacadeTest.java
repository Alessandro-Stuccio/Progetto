package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.facade.impl.SubscriptionFacadeImpl;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.strategy.BookingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionFacadeTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private UserService userService;
    @Mock private PlanService planService;
    @Mock private BookingStrategy ptStrategy;
    @Mock private BookingStrategy nutriStrategy;

    private SubscriptionFacadeImpl subscriptionFacade;

    private User user;
    private Plan annualPlan;
    private Plan semestralPlan;

    @BeforeEach
    void setUp() {
        subscriptionFacade = new SubscriptionFacadeImpl(
                subscriptionService, userService, planService,
                List.of(ptStrategy, nutriStrategy));

        user = User.builder().id(1L).firstName("Mario").lastName("Rossi")
                .email("x@x.com").password("testpass").role(Role.CLIENT).build();

        annualPlan = Plan.builder().id(1L).name("Premium Annuale")
                .duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();

        semestralPlan = Plan.builder().id(2L).name("Base Semestrale")
                .duration(PlanDuration.SEMESTRALE)
                .monthlyCreditsPT(4).monthlyCreditsNutri(2)
                .fullPrice(500.0).monthlyInstallmentPrice(90.0).build();
    }

    // ── activateSubscription ─────────────────────────────────────────────────

    @Test
    @DisplayName("activateSubscription — piano annuale con unica soluzione: date +1 anno, nessuna rata")
    void activateSubscription_annualUnicaSoluzione() {
        when(userService.getUserById(1L)).thenReturn(user);
        when(planService.getPlanById(1L)).thenReturn(annualPlan);
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        Subscription result = subscriptionFacade.activateSubscription(
                new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE), 1L);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentCreditsPT()).isEqualTo(8);
        assertThat(result.getCurrentCreditsNutri()).isEqualTo(4);
        assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusYears(1));
        assertThat(result.getInstallmentsPaid()).isEqualTo(1);
        assertThat(result.getTotalInstallments()).isEqualTo(1);
        assertThat(result.getNextPaymentDate()).isNull();
    }

    @Test
    @DisplayName("activateSubscription — piano semestrale con rate mensili: date +6 mesi, nextPaymentDate impostata")
    void activateSubscription_semestralRate() {
        when(userService.getUserById(1L)).thenReturn(user);
        when(planService.getPlanById(2L)).thenReturn(semestralPlan);
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = subscriptionFacade.activateSubscription(
                new PlanRequest(2L, PaymentFrequency.RATE_MENSILI), 1L);

        assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusMonths(6));
        assertThat(result.getNextPaymentDate()).isEqualTo(LocalDate.now().plusMonths(1));
        assertThat(result.getTotalInstallments()).isEqualTo(semestralPlan.getDuration().getMonths());
    }

    @Test
    @DisplayName("activateSubscription — disattiva abbonamento precedente prima di crearne uno nuovo")
    void activateSubscription_deactivatesPrevious() {
        Subscription existingSub = Subscription.builder().id(50L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE).active(true).build();

        when(userService.getUserById(1L)).thenReturn(user);
        when(planService.getPlanById(1L)).thenReturn(annualPlan);
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.of(existingSub));
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            if (s.getId() == null) s.setId(100L);
            return s;
        });

        subscriptionFacade.activateSubscription(
                new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE), 1L);

        assertThat(existingSub.isActive()).isFalse();
        verify(subscriptionService, atLeast(2)).save(any());
    }

    @Test
    @DisplayName("activateSubscription — utente non trovato lancia ResourceNotFoundException")
    void activateSubscription_userNotFound() {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("Utente", 999L));

        assertThatThrownBy(() -> subscriptionFacade.activateSubscription(
                new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE), 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("activateSubscription — piano non trovato lancia ResourceNotFoundException")
    void activateSubscription_planNotFound() {
        when(userService.getUserById(1L)).thenReturn(user);
        when(planService.getPlanById(999L))
                .thenThrow(new ResourceNotFoundException("Piano", 999L));

        assertThatThrownBy(() -> subscriptionFacade.activateSubscription(
                new PlanRequest(999L, PaymentFrequency.UNICA_SOLUZIONE), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deductCredits ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deductCredits — strategia selezionata per ruolo, crediti scalati e abbonamento salvato")
    void deductCredits_success() {
        User pt = User.builder().id(2L).email("pt@test.com").password("testpass")
                .firstName("PT").lastName("One").role(Role.PERSONAL_TRAINER).build();
        Slot slot = Slot.builder().id(10L).professional(pt).bookedBy(user)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();
        Subscription sub = Subscription.builder().id(100L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE).active(true)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .currentCreditsPT(8).currentCreditsNutri(4).build();

        when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);
        when(subscriptionService.findActiveByUserWithLock(user)).thenReturn(Optional.of(sub));
        when(subscriptionService.save(sub)).thenReturn(sub);

        subscriptionFacade.deductCredits(slot);

        verify(ptStrategy).consumeCredits(sub);
        verify(subscriptionService).save(sub);
    }

    // ── refundCreditsIfActive ────────────────────────────────────────────────

    @Test
    @DisplayName("refundCreditsIfActive — nessun abbonamento attivo: nessun rimborso effettuato")
    void refundCreditsIfActive_noSubscription() {
        when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);
        when(subscriptionService.findActiveByUserWithLock(user)).thenReturn(Optional.empty());

        subscriptionFacade.refundCreditsIfActive(user, Role.PERSONAL_TRAINER);

        verify(subscriptionService, never()).save(any());
        verify(ptStrategy, never()).refundCredits(any());
    }

    @Test
    @DisplayName("refundCreditsIfActive — abbonamento attivo: crediti rimborsati e abbonamento salvato")
    void refundCreditsIfActive_success() {
        Subscription sub = Subscription.builder().id(100L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE).active(true)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .currentCreditsPT(7).currentCreditsNutri(4).build();

        when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);
        when(subscriptionService.findActiveByUserWithLock(user)).thenReturn(Optional.of(sub));
        when(subscriptionService.save(sub)).thenReturn(sub);

        subscriptionFacade.refundCreditsIfActive(user, Role.PERSONAL_TRAINER);

        verify(ptStrategy).refundCredits(sub);
        verify(subscriptionService).save(sub);
    }
}
