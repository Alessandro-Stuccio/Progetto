package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.stats.AdminStatsResponse;

import java.util.List;

/**
 * Operazioni amministrative: oltre a quanto offerto da ModeratorFacade, aggiunge
 * la gestione dei piani e le statistiche globali.
 */
public interface AdminFacade {

    PlanResponseDTO createPlan(PlanCreateRequestDTO request);

    PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request);

    /**
     * Tutti i piani, compresi quelli disabilitati, per la gestione amministrativa.
     */
    List<PlanResponseDTO> getAllPlansForAdmin();

    /**
     * Abilita o disabilita un piano (soft-disable: il record resta in DB). Disabilitare
     * è permesso solo se al piano non sono collegati abbonamenti.
     *
     * @throws IllegalStateException se il piano ha abbonamenti collegati
     */
    PlanResponseDTO setPlanStatus(Long id, boolean active);

    /**
     * Statistiche globali della piattaforma.
     */
    AdminStatsResponse getAdminStats();
}
