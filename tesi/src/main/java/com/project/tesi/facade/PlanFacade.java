package com.project.tesi.facade;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.model.Plan;

import java.util.List;

public interface PlanFacade {
    List<PlanResponseDTO> getAllPlans();
    PlanResponseDTO getPlanById(Long id);
    PlanResponseDTO createPlan(Plan plan);
    PlanResponseDTO updatePlan(Long id, Plan updated);
    void deletePlan(Long id);
}
