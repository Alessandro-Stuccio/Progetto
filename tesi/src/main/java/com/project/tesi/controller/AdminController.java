package com.project.tesi.controller;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.facade.AdminFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST per le operazioni di amministrazione.
 * Espone /api/admin. Richiede ruolo ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }


    /**
     * Crea un nuovo piano di abbonamento.
     *
     * @param request dati del piano da creare
     * @return {@link PlanResponseDTO} del piano appena creato
     */
    @PostMapping("/plans")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.createPlan(request));
    }

    /**
     * Aggiorna un piano di abbonamento esistente.
     *
     * @param id      identificativo del piano da aggiornare
     * @param request nuovi dati del piano
     * @return {@link PlanResponseDTO} aggiornato
     */
    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@PathVariable Long id,
            @Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.updatePlan(id, request));
    }

    /**
     * Elimina un piano di abbonamento.
     *
     * @param id identificativo del piano da eliminare
     * @return messaggio di conferma operazione
     */
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        adminFacade.deletePlan(id);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }

    /**
     * Restituisce statistiche aggregate per la dashboard dell'amministratore
     * (utenti attivi, prenotazioni, abbonamenti, ecc.).
     *
     * @return {@link AdminStatsResponse} con i dati statistici
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminFacade.getAdminStats());
    }
}
