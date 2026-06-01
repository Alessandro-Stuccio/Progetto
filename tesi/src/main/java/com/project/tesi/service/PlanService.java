package com.project.tesi.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.Plan;
import java.util.List;

/** Gestione dei piani di abbonamento. */
@Validated
public interface PlanService {

    /** Tutti i piani, anche quelli disabilitati: serve alla vista admin e alle statistiche. */
    List<Plan> getAllPlans();

    /** Solo i piani attivi, per la vista pubblica/client. */
    List<Plan> getActivePlans();

    Plan getPlanById(@NotNull @Min(1) Long id);

    Plan createPlan(@NotNull Plan plan);

    /** Attiva o disabilita un piano. È un soft-disable: il record resta in DB. */
    Plan setActive(@NotNull @Min(1) Long id, boolean active);

    boolean existsByName(@NotNull String name);
}
