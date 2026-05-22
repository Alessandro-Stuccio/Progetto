package com.project.tesi.facade.impl;

import com.project.tesi.facade.PlanFacade;
import com.project.tesi.model.Plan;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementazione del facade per i piani di abbonamento.
 */
@Component
public class PlanFacadeImpl implements PlanFacade {

    private final PlanService planService;

    public PlanFacadeImpl(PlanService planService) {
        this.planService = planService;
    }

    @Override
    public List<Plan> getAllPlans() {
        return planService.getAllPlans();
    }

    @Override
    public Plan getPlanById(Long id) {
        return planService.getPlanById(id);
    }

    @Override
    public Plan createPlan(Plan plan) {
        return planService.createPlan(plan);
    }

    @Override
    public Plan updatePlan(Long id, Plan updated) {
        return planService.updatePlan(id, updated);
    }

    @Override
    public void deletePlan(Long id) {
        planService.deletePlan(id);
    }
}
