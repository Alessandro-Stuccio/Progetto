package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.facade.impl.AdminFacadeImpl;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFacadeTest {

    @Mock private ChatService chatService;
    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private PlanService planService;
    @Mock private SlotService slotService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private PlanMapper planMapper;
    @Mock private SubscriptionFacade subscriptionFacade;

    @InjectMocks
    private AdminFacadeImpl adminFacade;

    @Test
    @DisplayName("updateSubscriptionCredits — crediti negativi lancia IllegalArgumentException")
    void updateSubscriptionCredits_negativePT_throws() {
        assertThatThrownBy(() -> adminFacade.updateSubscriptionCredits(1L, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        verify(subscriptionService, never()).updateSubscriptionCredits(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("createPlan — delega al service e mappa")
    void createPlan() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO("Premium", "ANNUALE", 100.0, 100.0, 5, 5);
        Plan plan = Plan.builder().id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .fullPrice(100.0).monthlyInstallmentPrice(100.0).build();
        PlanResponseDTO response = PlanResponseDTO.builder().id(1L).name("Premium").duration("ANNUALE")
                .fullPrice(100.0).monthlyInstallmentPrice(100.0).monthlyCreditsPT(5).monthlyCreditsNutri(5).build();

        when(planService.existsByName("Premium")).thenReturn(false);
        when(planMapper.toPlan(request)).thenReturn(plan);
        when(planService.createPlan(any())).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(response);

        PlanResponseDTO result = adminFacade.createPlan(request);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createPlan — campi mancanti lancia IllegalArgumentException")
    void createPlan_missingFields_throws() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO(null, "ANNUALE", 100.0, 100.0, 5, 5);
        assertThatThrownBy(() -> adminFacade.createPlan(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("createPlan — nome duplicato lancia ResourceAlreadyExistsException")
    void createPlan_duplicateName_throws() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO("Premium", "ANNUALE", 100.0, 100.0, 5, 5);
        when(planService.existsByName("Premium")).thenReturn(true);
        assertThatThrownBy(() -> adminFacade.createPlan(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("deletePlan — piano con sottoscrittori attivi lancia IllegalStateException")
    void deletePlan_hasSubscribers_throws() {
        when(subscriptionService.hasSubscribersByPlan(1L)).thenReturn(true);
        assertThatThrownBy(() -> adminFacade.deletePlan(1L))
                .isInstanceOf(IllegalStateException.class);
        verify(planService, never()).deletePlan(any());
    }

    @Test
    @DisplayName("deletePlan — delega al service")
    void deletePlan() {
        when(subscriptionService.hasSubscribersByPlan(1L)).thenReturn(false);
        adminFacade.deletePlan(1L);
        verify(planService).deletePlan(1L);
    }

    @Test
    @DisplayName("getAllSubscriptions — mappa abbonamenti a DTO")
    void getAllSubscriptions() {
        Subscription sub = new Subscription();
        sub.setId(1L);
        SubscriptionResponse response = SubscriptionResponse.builder().id(1L).build();
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sub));
        when(subscriptionMapper.toResponse(sub)).thenReturn(response);

        List<SubscriptionResponse> result = adminFacade.getAllSubscriptions();
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAdminStats — aggrega dati dai service")
    void getAdminStats() {
        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = adminFacade.getAdminStats();
        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isEqualTo(0);
        assertThat(result.getTotalActiveSubscriptions()).isEqualTo(0L);
    }
}
