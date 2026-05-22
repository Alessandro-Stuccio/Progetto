package com.project.tesi.service.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitari per {@link SubscriptionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PlanRepository planRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Plan annualPlan;
    private Plan semestralPlan;

    @BeforeEach
    void setUp() {
        user = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(1L).email("x@x.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).firstName("Mario").lastName("Rossi").build();

        annualPlan = Plan.builder().name("plan").duration(com.project.tesi.enums.PlanDuration.ANNUALE).fullPrice(100.0).monthlyInstallmentPrice(10.0).id(1L).name("Premium Annuale")
                .duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();

        semestralPlan = Plan.builder().name("plan").duration(com.project.tesi.enums.PlanDuration.ANNUALE).fullPrice(100.0).monthlyInstallmentPrice(10.0).id(2L).name("Base Semestrale")
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
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(
                SubscriptionResponse.builder().planName("Premium Annuale").active(true)
                        .currentCreditsPT(8).currentCreditsNutri(4).build());

        SubscriptionResponse response = subscriptionService.activateSubscription(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getPlanName()).isEqualTo("Premium Annuale");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getCurrentCreditsPT()).isEqualTo(8);
        assertThat(response.getCurrentCreditsNutri()).isEqualTo(4);
    }

    @Test
    @DisplayName("activateSubscription — piano semestrale con rate mensili")
    void activateSubscription_semestralRate() {
        PlanRequest request = new PlanRequest(2L, PaymentFrequency.RATE_MENSILI);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(2L)).thenReturn(Optional.of(semestralPlan));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(
                SubscriptionResponse.builder().planName("Base Semestrale").active(true).build());

        SubscriptionResponse response = subscriptionService.activateSubscription(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getPlanName()).isEqualTo("Base Semestrale");
    }

    @Test
    @DisplayName("activateSubscription — disattiva abbonamento precedente")
    void activateSubscription_deactivatesPrevious() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);

        Subscription existingSub = Subscription.builder().user(new com.project.tesi.model.User()).plan(new com.project.tesi.model.Plan()).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE).id(50L).user(user).plan(annualPlan).paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE).active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(annualPlan));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            if (s.getId() == null) s.setId(100L);
            return s;
        });
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(
                SubscriptionResponse.builder().active(true).build());

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
    @DisplayName("getSubscriptionStatus — restituisce lo stato dell'abbonamento attivo")
    void getSubscriptionStatus_success() {
        Subscription sub = Subscription.builder().user(new com.project.tesi.model.User()).plan(new com.project.tesi.model.Plan()).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE).id(100L).user(user).plan(annualPlan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .currentCreditsPT(8).currentCreditsNutri(4).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(sub));
        when(subscriptionMapper.toResponse(sub)).thenReturn(
                SubscriptionResponse.builder().active(true).planName("Premium Annuale").build());

        SubscriptionResponse response = subscriptionService.getSubscriptionStatus(1L);

        assertThat(response).isNotNull();
        assertThat(response.isActive()).isTrue();
        assertThat(response.getPlanName()).isEqualTo("Premium Annuale");
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

