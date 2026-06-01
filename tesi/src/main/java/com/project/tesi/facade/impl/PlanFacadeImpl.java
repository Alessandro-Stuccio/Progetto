package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.facade.PlanFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
@Component
public class PlanFacadeImpl implements PlanFacade {

    private final PlanService planService;
    private final PlanMapper planMapper;

    public PlanFacadeImpl(PlanService planService, PlanMapper planMapper) {
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> getAllPlans() {
        return planMapper.toResponseList(planService.getActivePlans());
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDTO getPlanById(Long id) {
        return planMapper.toResponse(planService.getPlanById(id));
    }

    @Override
    @Transactional
    public PlanResponseDTO createPlan(Plan plan) {
        return planMapper.toResponse(planService.createPlan(plan));
    }

    @Override
    @Transactional
    public PlanResponseDTO updatePlan(Long id, Plan updated) {
        Plan existing = planService.getPlanById(id);
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDuration() != null) existing.setDuration(updated.getDuration());
        if (updated.getFullPrice() != null) existing.setFullPrice(updated.getFullPrice());
        if (updated.getMonthlyInstallmentPrice() != null) existing.setMonthlyInstallmentPrice(updated.getMonthlyInstallmentPrice());
        existing.setMonthlyCreditsPT(updated.getMonthlyCreditsPT());
        existing.setMonthlyCreditsNutri(updated.getMonthlyCreditsNutri());
        return planMapper.toResponse(planService.createPlan(existing));
    }
}
