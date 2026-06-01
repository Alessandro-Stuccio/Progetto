package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanFacadeImpl unit tests")
class PlanFacadeImplTest {

    @Mock private PlanService planService;
    @Mock private PlanMapper planMapper;

    @InjectMocks
    private PlanFacadeImpl planFacade;

    private Plan plan;
    private PlanResponseDTO planResponseDTO;

    @BeforeEach
    void setUp() {
        plan = new Plan();
        plan.setId(1L);
        plan.setName("Basic");
        plan.setDuration(PlanDuration.SEMESTRALE);
        plan.setFullPrice(100.0);
        plan.setMonthlyInstallmentPrice(20.0);
        plan.setMonthlyCreditsPT(1);
        plan.setMonthlyCreditsNutri(1);

        planResponseDTO = PlanResponseDTO.builder()
                .id(1L).name("Basic").duration("SEMESTRALE")
                .fullPrice(100.0).monthlyInstallmentPrice(20.0)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1)
                .build();
    }

    // ─── getAllPlans ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPlans: delegates to service (active only) and mapper, returns list")
    void getAllPlans_returnsMappedList() {
        List<Plan> plans = List.of(plan);
        List<PlanResponseDTO> expected = List.of(planResponseDTO);
        when(planService.getActivePlans()).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expected);

        List<PlanResponseDTO> result = planFacade.getAllPlans();

        assertThat(result).isEqualTo(expected);
        verify(planService).getActivePlans();
        verify(planMapper).toResponseList(plans);
    }

    @Test
    @DisplayName("getAllPlans: empty service result returns empty list")
    void getAllPlans_emptyList_returnsEmpty() {
        when(planService.getActivePlans()).thenReturn(List.of());
        when(planMapper.toResponseList(List.of())).thenReturn(List.of());

        List<PlanResponseDTO> result = planFacade.getAllPlans();

        assertThat(result).isEmpty();
    }

    // ─── getPlanById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPlanById: delegates to service and mapper, returns DTO")
    void getPlanById_returnsMappedDTO() {
        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = planFacade.getPlanById(1L);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planService).getPlanById(1L);
        verify(planMapper).toResponse(plan);
    }

    // ─── createPlan ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPlan: saves through service and maps to response")
    void createPlan_savesAndReturnsDTO() {
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = planFacade.createPlan(plan);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planService).createPlan(plan);
        verify(planMapper).toResponse(plan);
    }

    // ─── updatePlan ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePlan: applies non-null name from update, calls createPlan to save")
    void updatePlan_withNewName_updatesNameAndSaves() {
        Plan update = new Plan();
        update.setName("Premium");
        update.setDuration(PlanDuration.ANNUALE);
        update.setFullPrice(200.0);
        update.setMonthlyInstallmentPrice(30.0);
        update.setMonthlyCreditsPT(2);
        update.setMonthlyCreditsNutri(2);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        planFacade.updatePlan(1L, update);

        assertThat(plan.getName()).isEqualTo("Premium");
        assertThat(plan.getDuration()).isEqualTo(PlanDuration.ANNUALE);
        assertThat(plan.getFullPrice()).isEqualTo(200.0);
        assertThat(plan.getMonthlyInstallmentPrice()).isEqualTo(30.0);
        assertThat(plan.getMonthlyCreditsPT()).isEqualTo(2);
        assertThat(plan.getMonthlyCreditsNutri()).isEqualTo(2);
        verify(planService).createPlan(plan);
    }

    @Test
    @DisplayName("updatePlan: null name in update does not overwrite existing name")
    void updatePlan_nullName_keepsExistingName() {
        Plan update = new Plan();
        update.setName(null);
        update.setDuration(PlanDuration.ANNUALE);
        update.setFullPrice(200.0);
        update.setMonthlyInstallmentPrice(30.0);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        planFacade.updatePlan(1L, update);

        assertThat(plan.getName()).isEqualTo("Basic");
    }

    @Test
    @DisplayName("updatePlan: credits are always applied, even when zero")
    void updatePlan_zeroCredits_appliedToExisting() {
        Plan update = new Plan();
        update.setName(null);
        update.setDuration(null);
        update.setFullPrice(null);
        update.setMonthlyInstallmentPrice(null);
        update.setMonthlyCreditsPT(0);
        update.setMonthlyCreditsNutri(0);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        planFacade.updatePlan(1L, update);

        assertThat(plan.getMonthlyCreditsPT()).isEqualTo(0);
        assertThat(plan.getMonthlyCreditsNutri()).isEqualTo(0);
    }

}
