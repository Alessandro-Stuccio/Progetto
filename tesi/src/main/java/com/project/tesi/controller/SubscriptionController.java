package com.project.tesi.controller;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.facade.UserFacade;
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
public class SubscriptionController {

    private final UserFacade userFacade;

    public SubscriptionController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * Attiva un nuovo abbonamento per il cliente autenticato con il piano e la frequenza di pagamento scelti.
     *
     * @param request contiene il piano (BASIC/PREMIUM), la durata e la modalità di pagamento
     * @param user    cliente autenticato che attiva l'abbonamento
     * @return il {@link SubscriptionResponse} dell'abbonamento attivato
     */
    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(@Valid @RequestBody PlanRequest request,
                                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.activateSubscription(request, user.getId()));
    }

    /**
     * Restituisce i crediti residui e i dettagli dell'abbonamento attivo del cliente autenticato.
     *
     * @param user cliente autenticato di cui recuperare lo stato abbonamento
     * @return il {@link SubscriptionResponse} con crediti, data di scadenza e piano
     */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionResponse> getSubscriptionStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getSubscriptionStatus(user.getId()));
    }
}
