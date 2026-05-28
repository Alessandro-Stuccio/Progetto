package com.project.tesi.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.Plan;
import java.util.List;

@Validated
public interface PlanService {

    List<Plan> getAllPlans();

    Plan getPlanById(@NotNull @Min(1) Long id);

    Plan createPlan(@NotNull Plan plan);

    void deletePlan(@NotNull @Min(1) Long id);

    boolean existsByName(@NotNull String name);
}
