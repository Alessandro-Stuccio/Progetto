package com.project.tesi.service.impl;

import com.project.tesi.dto.response.stats.AdminStatsResponse;
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

import java.time.LocalDateTime;
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
        pt = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(2L).firstName("Luca").lastName("Bianchi")
                .role(Role.PERSONAL_TRAINER).createdAt(LocalDateTime.now().minusDays(30)).build();
        client = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi")
                .role(Role.CLIENT).assignedPT(pt)
                .createdAt(LocalDateTime.now().minusDays(10)).build();
        plan = Plan.builder().name("plan").duration(com.project.tesi.enums.PlanDuration.ANNUALE).fullPrice(100.0).monthlyInstallmentPrice(10.0).id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();
        sub = Subscription.builder().user(new com.project.tesi.model.User()).plan(new com.project.tesi.model.Plan()).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE).id(1L).user(client).plan(plan).active(true)
                .currentCreditsPT(5).currentCreditsNutri(2).build();
    }

    @Test @DisplayName("getAdminStats — restituisce statistiche complete")
    void getAdminStats_success() {
        when(userRepository.findAll()).thenReturn(List.of(client, pt));
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        when(planRepository.findAll()).thenReturn(List.of(plan));
        when(slotRepository.findAllBooked()).thenReturn(List.of());

        AdminStatsResponse stats = adminStatsService.getAdminStats();

        assertThat(stats.totalUsers()).isEqualTo(2);
        assertThat(stats.usersByRole()).isNotNull();
        assertThat(stats.usersPerMonth()).isNotNull();
        assertThat(stats.planPopularity()).isNotNull();
        assertThat(stats.credits()).isNotNull();
        assertThat(stats.monthlyRevenue()).isGreaterThanOrEqualTo(0.0);
        assertThat(stats.yearlyRevenue()).isGreaterThanOrEqualTo(0.0);
        assertThat(stats.bookingsThisMonth()).isGreaterThanOrEqualTo(0L);
        assertThat(stats.professionalWorkload()).isNotNull();
        assertThat(stats.totalActiveSubscriptions()).isEqualTo(1L);
    }

    @Test @DisplayName("getAdminStats — senza dati restituisce statistiche vuote")
    void getAdminStats_empty() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        when(planRepository.findAll()).thenReturn(List.of());
        when(slotRepository.findAllBooked()).thenReturn(List.of());

        AdminStatsResponse stats = adminStatsService.getAdminStats();

        assertThat(stats.totalUsers()).isEqualTo(0);
        assertThat(stats.totalActiveSubscriptions()).isEqualTo(0L);
        assertThat(stats.monthlyRevenue()).isEqualTo(0.0);
    }

    @Test @DisplayName("getAdminStats — crediti con percentuale d'uso corretta")
    void getAdminStats_credits() {
        when(userRepository.findAll()).thenReturn(List.of(client));
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        when(planRepository.findAll()).thenReturn(List.of(plan));
        when(slotRepository.findAllBooked()).thenReturn(List.of());

        AdminStatsResponse stats = adminStatsService.getAdminStats();
        AdminStatsResponse.CreditsStats credits = stats.credits();

        assertThat(credits.ptAvailable()).isEqualTo(5);
        assertThat(credits.ptTotal()).isEqualTo(8);
        assertThat(credits.ptConsumed()).isEqualTo(3);
    }

    @Test @DisplayName("getAdminStats — nutrizionista nel carico professionisti")
    void getAdminStats_nutriWorkload() {
        User nutri = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(3L).firstName("Sara").lastName("Verdi")
                .role(Role.NUTRITIONIST).createdAt(LocalDateTime.now()).build();
        User clientNutri = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(4L).firstName("Anna").lastName("Neri")
                .role(Role.CLIENT).assignedNutritionist(nutri).createdAt(LocalDateTime.now()).build();

        when(userRepository.findAll()).thenReturn(List.of(nutri, clientNutri));
        when(subscriptionRepository.findAll()).thenReturn(List.of());
        when(planRepository.findAll()).thenReturn(List.of());
        when(slotRepository.findAllBooked()).thenReturn(List.of());

        AdminStatsResponse stats = adminStatsService.getAdminStats();
        assertThat(stats.professionalWorkload()).hasSize(1);
        assertThat(stats.professionalWorkload().get(0).clientCount()).isEqualTo(1L);
    }
}
