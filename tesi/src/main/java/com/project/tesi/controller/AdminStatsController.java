package com.project.tesi.controller;

import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.facade.AdminFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per le statistiche admin. Aggrega i dati globali della piattaforma.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Stats", description = "Statistiche aggregate per la dashboard dell'amministratore")
public class AdminStatsController {

    private final AdminFacade adminFacade;

    public AdminStatsController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @Operation(summary = "Statistiche aggregate per la dashboard admin")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminFacade.getAdminStats());
    }
}
