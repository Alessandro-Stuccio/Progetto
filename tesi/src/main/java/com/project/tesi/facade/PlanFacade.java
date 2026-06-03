package com.project.tesi.facade;

import com.project.tesi.dto.response.PlanResponseDTO;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
public interface PlanFacade {

    /**
     * I soli piani attivi, per la vista pubblica/cliente.
     *
     * @return i piani attualmente attivi
     */
    List<PlanResponseDTO> getAllPlans();
}
