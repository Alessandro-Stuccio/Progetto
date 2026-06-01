package com.project.tesi.facade;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.model.Plan;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
public interface PlanFacade {

    /**
     * I soli piani attivi, per la vista pubblica/cliente.
     */
    List<PlanResponseDTO> getAllPlans();

    PlanResponseDTO getPlanById(Long id);

    PlanResponseDTO createPlan(Plan plan);

    PlanResponseDTO updatePlan(Long id, Plan updated);
}
