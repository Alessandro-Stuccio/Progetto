package com.project.tesi.controller;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.SubscriptionCreditsUpdateDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST per le operazioni di amministrazione.
 * Espone /api/admin. Richiede ruolo ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "API di amministrazione per utenti, abbonamenti e piani")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    /**
     * Recupera tutti gli utenti gestibili dall'amministratore corrente.
     *
     * @param user amministratore autenticato
     * @return lista di {@link UserResponse}
     */
    @Operation(summary = "Recupera tutti gli utenti gestibili")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getManageableUsers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.getManageableUsers(user));
    }

    /**
     * Crea un nuovo utente nel sistema.
     *
     * @param body dati del nuovo utente
     * @param user amministratore autenticato che esegue l'operazione
     * @return {@link UserResponse} con i dati dell'utente creato
     */
    @Operation(summary = "Crea un nuovo utente")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.createUser(body, user));
    }

    /**
     * Aggiorna i dati di un utente esistente.
     *
     * @param id   identificativo dell'utente da aggiornare
     * @param body nuovi dati dell'utente
     * @param user amministratore autenticato che esegue l'operazione
     * @return {@link UserResponse} con i dati aggiornati
     */
    @Operation(summary = "Aggiorna un utente esistente")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.updateUser(id, body, user));
    }

    /**
     * Disabilita un utente tramite soft delete (non viene rimosso dal database).
     *
     * @param id   identificativo dell'utente da disabilitare
     * @param user amministratore autenticato che esegue l'operazione
     * @return messaggio di conferma operazione
     */
    @Operation(summary = "Disabilita un utente (soft delete)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        adminFacade.deleteUser(id, user);
        return ResponseEntity.ok(Map.of("message", "Utente disabilitato"));
    }

    /**
     * Recupera i contatti disponibili per la chat dell'amministratore.
     *
     * @return lista di {@link UserResponse} contattabili
     */
    @Operation(summary = "Recupera i contatti per la chat")
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(adminFacade.getChatContacts());
    }

    /**
     * Recupera tutti gli abbonamenti presenti nel sistema.
     *
     * @return lista di {@link SubscriptionResponse}
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(adminFacade.getAllSubscriptions());
    }

    /**
     * Aggiorna manualmente i crediti di un abbonamento.
     *
     * @param id      identificativo dell'abbonamento
     * @param request nuovi valori dei crediti PT e nutrizionista
     * @return {@link SubscriptionResponse} aggiornato
     */
    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody SubscriptionCreditsUpdateDTO request) {
        return ResponseEntity.ok(adminFacade.updateSubscriptionCredits(
                id,
                request.creditsPT() != null ? request.creditsPT() : 0,
                request.creditsNutri() != null ? request.creditsNutri() : 0
        ));
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
    @Operation(summary = "Statistiche aggregate per la dashboard admin")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminFacade.getAdminStats());
    }
}
