package com.project.tesi.facade;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.model.Plan;

import java.util.List;

/**
 * Facade per la gestione dei piani di abbonamento disponibili.
 */
public interface PlanFacade {

    /**
     * Restituisce tutti i piani di abbonamento disponibili.
     *
     * @return lista di tutti i piani
     */
    List<PlanResponseDTO> getAllPlans();

    /**
     * Recupera un piano di abbonamento tramite il suo identificativo.
     *
     * @param id identificativo del piano
     * @return dettagli del piano richiesto
     */
    PlanResponseDTO getPlanById(Long id);

    /**
     * Crea un nuovo piano di abbonamento.
     *
     * @param plan entità piano da creare
     * @return il piano creato
     */
    PlanResponseDTO createPlan(Plan plan);

    /**
     * Aggiorna un piano di abbonamento esistente.
     *
     * @param id      identificativo del piano da aggiornare
     * @param updated entità piano con i nuovi dati
     * @return il piano aggiornato
     */
    PlanResponseDTO updatePlan(Long id, Plan updated);

    /**
     * Elimina un piano di abbonamento.
     *
     * @param id identificativo del piano da eliminare
     */
    void deletePlan(Long id);
}
