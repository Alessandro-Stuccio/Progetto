package com.project.tesi.facade;

import com.project.tesi.model.Plan;

import java.util.List;

public interface PlanFacade {
    List<Plan> getAllPlans();
    Plan getPlanById(Long id);
    Plan createPlan(Plan plan);
    Plan updatePlan(Long id, Plan updated);
    void deletePlan(Long id);
}
