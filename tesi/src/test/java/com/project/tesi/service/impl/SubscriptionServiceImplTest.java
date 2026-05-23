package com.project.tesi.service.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.strategy.BookingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PlanRepository planRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingStrategy bookingStrategy;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Plan annualPlan;
    private Plan semestralPlan;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("activateSubscription — piano annuale con unica soluzione")
    void activateSubscription_annualUnicaSoluzione() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(annualPlan));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        Subscription result = subscriptionService.activateSubscription(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentCreditsPT()).isEqualTo(8);
        assertThat(result.getCurrentCreditsNutri()).isEqualTo(4);
        assertThat(result.getPlan().getName()).isEqualTo("Premium Annuale");
    }

    @Test
    @DisplayName("activateSubscription — piano semestrale con rate mensili")
    void activateSubscription_semestralRate() {
        PlanRequest request = new PlanRequest(2L, PaymentFrequency.RATE_MENSILI);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(2L)).thenReturn(Optional.of(semestralPlan));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = subscriptionService.activateSubscription(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPlan().getName()).isEqualTo("Base Semestrale");
        assertThat(result.getNextPaymentDate()).isNotNull();
    }

    @Test
    @DisplayName("activateSubscription — disattiva abbonamento precedente")
    void activateSubscription_deactivatesPrevious() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Subscription existingSub = Subscription.builder().id(50L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE).active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(annualPlan));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            if (s.getId() == null) s.setId(100L);
            return s;
        });

        subscriptionService.activateSubscription(request, 1L);

        assertThat(existingSub.isActive()).isFalse();
        verify(subscriptionRepository, atLeast(2)).save(any());
    }

    @Test
    @DisplayName("activateSubscription — utente non trovato")
    void activateSubscription_userNotFound() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.activateSubscription(request, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("activateSubscription — piano non trovato")
    void activateSubscription_planNotFound() {
        PlanRequest request = new PlanRequest(999L, PaymentFrequency.UNICA_SOLUZIONE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.activateSubscription(request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getSubscriptionStatus — restituisce l'entità abbonamento attivo")
    void getSubscriptionStatus_success() {
        Subscription sub = Subscription.builder().id(100L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .currentCreditsPT(8).currentCreditsNutri(4).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(sub));

        Subscription result = subscriptionService.getSubscriptionStatus(1L);

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentCreditsPT()).isEqualTo(8);
    }

    @Test
    @DisplayName("getSubscriptionStatus — nessun abbonamento attivo lancia SubscriptionNotFoundException")
    void getSubscriptionStatus_noSubscription() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionStatus(1L))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    @DisplayName("getSubscriptionStatus — utente non trovato")
    void getSubscriptionStatus_userNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionStatus(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
