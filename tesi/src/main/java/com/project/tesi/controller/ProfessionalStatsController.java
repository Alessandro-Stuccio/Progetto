package com.project.tesi.controller;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.facade.ProfessionalFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per le metriche della dashboard del professionista autenticato.
 */
@RestController
@RequestMapping("/api/professional")
@Tag(name = "Professional Stats", description = "Metriche e statistiche per la dashboard del professionista")
public class ProfessionalStatsController {

    private final ProfessionalFacade professionalFacade;

    public ProfessionalStatsController(ProfessionalFacade professionalFacade) {
        this.professionalFacade = professionalFacade;
    }

    /** Restituisce tutte le statistiche aggregate per la dashboard del professionista autenticato. */
    @GetMapping("/stats")
    public ResponseEntity<ProfessionalStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(professionalFacade.getProfessionalStats(user.getId()));
    }

    /** Restituisce gli appuntamenti futuri del professionista autenticato per il calendario. */
    @Operation(summary = "Appuntamenti futuri", description = "Lista degli slot prenotati futuri del professionista autenticato.")
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(professionalFacade.getUpcomingBookings(user.getId()));
    }
}
