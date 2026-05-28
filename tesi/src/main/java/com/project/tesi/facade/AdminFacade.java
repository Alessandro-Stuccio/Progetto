package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.stats.AdminStatsResponse;

public interface AdminFacade extends ModeratorFacade {
    PlanResponseDTO createPlan(PlanCreateRequestDTO request);
    PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request);
    void deletePlan(Long id);
    AdminStatsResponse getAdminStats();
}
