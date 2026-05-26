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

    @Mock private SlotRepository slotRepository;
    @Mock private PlanRepository planRepository;

    @InjectMocks private AdminStatsServiceImpl adminStatsService;

    private User pt;
    private Plan plan;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").id(2L).firstName("Luca").lastName("Bianchi")
                .role(Role.PERSONAL_TRAINER).build();
        plan = Plan.builder().id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();
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
