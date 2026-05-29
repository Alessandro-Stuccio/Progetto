package com.project.tesi.service.impl;

import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Plan;
import com.project.tesi.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanServiceImpl planService;

    private Plan basicPlan;
    private Plan premiumPlan;

    @BeforeEach
    void setUp() {
        basicPlan = new Plan();
        basicPlan.setId(1L);
        basicPlan.setName("Basic Semestrale");
        basicPlan.setFullPrice(299.0);
        basicPlan.setMonthlyInstallmentPrice(59.0);
        basicPlan.setMonthlyCreditsPT(1);
        basicPlan.setMonthlyCreditsNutri(1);

        premiumPlan = new Plan();
        premiumPlan.setId(2L);
        premiumPlan.setName("Premium Annuale");
        premiumPlan.setFullPrice(799.0);
        premiumPlan.setMonthlyInstallmentPrice(79.0);
        premiumPlan.setMonthlyCreditsPT(2);
        premiumPlan.setMonthlyCreditsNutri(2);
    }

    // ---- getAllPlans ----

    @Test
    @DisplayName("getAllPlans: returns all plans from repository")
    void getAllPlans_returnsAllPlans() {
        when(planRepository.findAll()).thenReturn(List.of(basicPlan, premiumPlan));

        List<Plan> result = planService.getAllPlans();

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(basicPlan, premiumPlan);
        verify(planRepository).findAll();
    }

    @Test
    @DisplayName("getAllPlans: returns empty list when no plans have been created")
    void getAllPlans_noPlans_returnsEmpty() {
        when(planRepository.findAll()).thenReturn(List.of());

        assertThat(planService.getAllPlans()).isEmpty();
    }

    // ---- getPlanById ----

    @Test
    @DisplayName("getPlanById: returns plan when found by id")
    void getPlanById_found_returnsPlan() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(basicPlan));

        Plan result = planService.getPlanById(1L);

        assertThat(result).isSameAs(basicPlan);
    }

    @Test
    @DisplayName("getPlanById: throws ResourceNotFoundException when plan id does not exist")
    void getPlanById_notFound_throwsResourceNotFoundException() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.getPlanById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- createPlan ----

    @Test
    @DisplayName("createPlan: persists plan and returns the saved entity")
    void createPlan_persistsAndReturnsPlan() {
        when(planRepository.save(basicPlan)).thenReturn(basicPlan);

        Plan result = planService.createPlan(basicPlan);

        assertThat(result).isSameAs(basicPlan);
        verify(planRepository).save(basicPlan);
    }

    // ---- deletePlan ----

    @Test
    @DisplayName("deletePlan: deletes plan when it exists")
    void deletePlan_exists_callsDeleteById() {
        when(planRepository.existsById(1L)).thenReturn(true);

        planService.deletePlan(1L);

        verify(planRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePlan: throws ResourceNotFoundException when plan id does not exist")
    void deletePlan_notFound_throwsResourceNotFoundException() {
        when(planRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> planService.deletePlan(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(planRepository, never()).deleteById(any());
    }

    // ---- existsByName ----

    @Test
    @DisplayName("existsByName: returns true when a plan with that name already exists")
    void existsByName_planExists_returnsTrue() {
        when(planRepository.findByName("Basic Semestrale")).thenReturn(Optional.of(basicPlan));

        assertThat(planService.existsByName("Basic Semestrale")).isTrue();
        verify(planRepository).findByName("Basic Semestrale");
    }

    @Test
    @DisplayName("existsByName: returns false when no plan with that name exists")
    void existsByName_noMatch_returnsFalse() {
        when(planRepository.findByName("Gold Annuale")).thenReturn(Optional.empty());

        assertThat(planService.existsByName("Gold Annuale")).isFalse();
    }
}
