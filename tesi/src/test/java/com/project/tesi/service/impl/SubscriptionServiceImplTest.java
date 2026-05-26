package com.project.tesi.service.impl;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.subscription.SubscriptionNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.SubscriptionRepository;
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

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Plan plan;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Mario").lastName("Rossi")
                .email("x@x.com").password("testpass").role(Role.CLIENT).build();

        plan = Plan.builder().id(1L).name("Premium Annuale")
                .duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();

        subscription = Subscription.builder().id(100L).user(user).plan(plan)
                .paymentFrequency(PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .currentCreditsPT(8).currentCreditsNutri(4).build();
    }

    // ── getSubscriptionStatus ────────────────────────────────────────────────

    @Test
    @DisplayName("getSubscriptionStatus — restituisce l'entità abbonamento attivo")
    void getSubscriptionStatus_success() {
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(subscription));

        Subscription result = subscriptionService.getSubscriptionStatus(user);

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentCreditsPT()).isEqualTo(8);
    }

    @Test
    @DisplayName("getSubscriptionStatus — nessun abbonamento attivo lancia SubscriptionNotFoundException")
    void getSubscriptionStatus_noSubscription() {
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionStatus(user))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save — persiste e restituisce l'abbonamento")
    void save_success() {
        when(subscriptionRepository.save(subscription)).thenReturn(subscription);

        Subscription result = subscriptionService.save(subscription);

        assertThat(result).isSameAs(subscription);
        verify(subscriptionRepository).save(subscription);
    }

    // ── findActiveByUser ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findActiveByUser — abbonamento attivo trovato")
    void findActiveByUser_found() {
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.of(subscription));

        Optional<Subscription> result = subscriptionService.findActiveByUser(user);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("findActiveByUser — nessun abbonamento attivo restituisce Optional vuoto")
    void findActiveByUser_empty() {
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.findActiveByUser(user);

        assertThat(result).isEmpty();
    }

    // ── findActiveByUserWithLock ─────────────────────────────────────────────

    @Test
    @DisplayName("findActiveByUserWithLock — abbonamento trovato con lock pessimistico")
    void findActiveByUserWithLock_found() {
        when(subscriptionRepository.findByUserAndActiveTrueWithLock(user)).thenReturn(Optional.of(subscription));

        Optional<Subscription> result = subscriptionService.findActiveByUserWithLock(user);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("findActiveByUserWithLock — nessun abbonamento restituisce Optional vuoto")
    void findActiveByUserWithLock_empty() {
        when(subscriptionRepository.findByUserAndActiveTrueWithLock(user)).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.findActiveByUserWithLock(user);

        assertThat(result).isEmpty();
    }
}
