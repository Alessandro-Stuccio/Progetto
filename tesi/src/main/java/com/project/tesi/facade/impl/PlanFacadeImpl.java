package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.facade.PlanFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanFacadeImpl implements PlanFacade {

    private final PlanService planService;
    private final PlanMapper planMapper;

    public PlanFacadeImpl(PlanService planService, PlanMapper planMapper) {
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @Override
    public List<PlanResponseDTO> getAllPlans() {
        return planMapper.toResponseList(planService.getAllPlans());
    }

    @Override
    public PlanResponseDTO getPlanById(Long id) {
        return planMapper.toResponse(planService.getPlanById(id));
    }

    @Override
    public PlanResponseDTO createPlan(Plan plan) {
        return planMapper.toResponse(planService.createPlan(plan));
    }

    @Override
    public PlanResponseDTO updatePlan(Long id, Plan updated) {
        return planMapper.toResponse(planService.updatePlan(id, updated));
    }

    @Override
    public void deletePlan(Long id) {
        planService.deletePlan(id);
    }
}
