package com.project.tesi.controller;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per la gestione abbonamenti e crediti residui.
 */
@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Attivazione e stato degli abbonamenti")
public class SubscriptionController {

    private final UserFacade userFacade;

    public SubscriptionController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Operation(summary = "Attiva abbonamento", description = "Attiva un nuovo abbonamento per il cliente autenticato con il piano e la modalità di pagamento scelti.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Abbonamento attivato"),
        @ApiResponse(responseCode = "400", description = "Piano non valido o abbonamento già attivo"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(@Valid @RequestBody PlanRequest request,
                                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.activateSubscription(request, user.getId()));
    }

    @Operation(summary = "Stato abbonamento", description = "Restituisce crediti residui, data di scadenza e dettagli del piano dell'abbonamento attivo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stato abbonamento"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "404", description = "Nessun abbonamento attivo")
    })
    @GetMapping("/status")
    public ResponseEntity<SubscriptionResponse> getSubscriptionStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getSubscriptionStatus(user.getId()));
    }
}
