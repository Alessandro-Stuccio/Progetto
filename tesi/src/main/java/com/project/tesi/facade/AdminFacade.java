package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.stats.AdminStatsResponse;

/**
 * Facade per le operazioni amministrative.
 * Estende {@link ModeratorFacade} aggiungendo gestione piani e statistiche globali.
 */
public interface AdminFacade extends ModeratorFacade {

    /**
     * Crea un nuovo piano di abbonamento.
     *
     * @param request dati del piano da creare
     * @return il piano creato
     */
    PlanResponseDTO createPlan(PlanCreateRequestDTO request);

    /**
     * Aggiorna un piano di abbonamento esistente.
     *
     * @param id      identificativo del piano da aggiornare
     * @param request nuovi dati del piano
     * @return il piano aggiornato
     */
    PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request);

    /**
     * Elimina un piano di abbonamento.
     *
     * @param id identificativo del piano da eliminare
     */
    void deletePlan(Long id);

    /**
     * Restituisce le statistiche globali della piattaforma per l'amministratore.
     *
     * @return riepilogo delle statistiche amministrative
     */
    AdminStatsResponse getAdminStats();
}
