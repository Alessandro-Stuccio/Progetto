package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.model.*;
import com.project.tesi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private PlanRepository planRepository;

    @InjectMocks private AdminStatsServiceImpl adminStatsService;

    private User client, pt;
    private Plan plan;
    private Subscription sub;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").id(2L).firstName("Luca").lastName("Bianchi")
                .role(Role.PERSONAL_TRAINER).build();
        client = User.builder().email("mario@test.com").password("testpass").id(1L).firstName("Mario").lastName("Rossi")
                .role(Role.CLIENT).assignedPT(pt).build();
        plan = Plan.builder().id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();
        sub = Subscription.builder().id(1L).user(client).plan(plan)
                .paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).currentCreditsPT(5).currentCreditsNutri(2).build();
    }

    @Test @DisplayName("getAllUsers — restituisce tutti gli utenti dal repository")
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(client, pt));
        List<User> result = adminStatsService.getAllUsers();
        assertThat(result).hasSize(2);
    }

    @Test @DisplayName("getAllUsers — lista vuota se nessun utente")
    void getAllUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThat(adminStatsService.getAllUsers()).isEmpty();
    }

    @Test @DisplayName("getAllSubscriptions — restituisce tutte le sottoscrizioni")
    void getAllSubscriptions() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        List<Subscription> result = adminStatsService.getAllSubscriptions();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlan().getName()).isEqualTo("Premium");
    }

    @Test @DisplayName("getAllSubscriptions — lista vuota se nessuna sottoscrizione")
    void getAllSubscriptions_empty() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        assertThat(adminStatsService.getAllSubscriptions()).isEmpty();
    }

    @Test @DisplayName("getAllPlans — restituisce tutti i piani")
    void getAllPlans() {
        when(planRepository.findAll()).thenReturn(List.of(plan));
        List<Plan> result = adminStatsService.getAllPlans();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Premium");
    }

    @Test @DisplayName("getAllBookedSlots — restituisce slot prenotati")
    void getAllBookedSlots() {
        Slot slot = Slot.builder().professional(pt)
                .startTime(java.time.LocalDateTime.now().plusDays(1))
                .endTime(java.time.LocalDateTime.now().plusDays(1).plusMinutes(30))
                .build();
        slot.setStatus(BookingStatus.CONFIRMED);
        when(slotRepository.findAllBooked()).thenReturn(List.of(slot));
        List<Slot> result = adminStatsService.getAllBookedSlots();
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getAllBookedSlots — lista vuota se nessuno slot prenotato")
    void getAllBookedSlots_empty() {
        when(slotRepository.findAllBooked()).thenReturn(List.of());
        assertThat(adminStatsService.getAllBookedSlots()).isEmpty();
    }
}
