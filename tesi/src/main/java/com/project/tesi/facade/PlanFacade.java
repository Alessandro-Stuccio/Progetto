package com.project.tesi.facade;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.exception.common.CustomResourceNotFoundException;
import com.project.tesi.model.Plan;

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

    /**
     * Recupera un piano dal suo id.
     *
     * @param id id del piano
     * @return il piano trovato
     * @throws CustomResourceNotFoundException se il piano non esiste
     */
    PlanResponseDTO getPlanById(Long id);

    /**
     * Crea un nuovo piano.
     *
     * @param plan il piano da creare
     * @return il piano creato
     */
    PlanResponseDTO createPlan(Plan plan);

    /**
     * Aggiorna un piano esistente.
     *
     * @param id      id del piano da aggiornare
     * @param updated dati aggiornati del piano
     * @return il piano aggiornato
     * @throws CustomResourceNotFoundException se il piano non esiste
     */
    PlanResponseDTO updatePlan(Long id, Plan updated);
}
