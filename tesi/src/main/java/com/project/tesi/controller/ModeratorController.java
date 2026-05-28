package com.project.tesi.controller;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UpdateCreditsRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.ModeratorFacade;
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
 * Controller REST per le operazioni del moderatore.
 * Espone /api/moderator. Richiede ruolo MODERATOR.
 */
@RestController
@RequestMapping("/api/moderator")
@Tag(name = "Moderator", description = "API per i moderatori")
public class ModeratorController {

    private final ModeratorFacade moderatorFacade;

    public ModeratorController(ModeratorFacade moderatorFacade) {
        this.moderatorFacade = moderatorFacade;
    }

    /**
     * Restituisce la lista degli utenti che il moderatore autenticato può gestire.
     *
     * @param user moderatore autenticato
     * @return lista di {@link UserResponse}
     */
    @Operation(summary = "Recupera gli utenti gestibili")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getManageableUsers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.getManageableUsers(user));
    }

    /**
     * Restituisce tutti gli abbonamenti presenti nel sistema.
     *
     * @return lista di {@link SubscriptionResponse}
     */
    @Operation(summary = "Recupera tutti gli abbonamenti")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(moderatorFacade.getAllSubscriptions());
    }

    /**
     * Restituisce i contatti disponibili per la chat del moderatore.
     *
     * @return lista di {@link UserResponse} con i contatti raggiungibili
     */
    @Operation(summary = "Recupera i contatti per la chat")
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(moderatorFacade.getChatContacts());
    }

    /**
     * Crea un nuovo utente nel sistema.
     *
     * @param body dati del nuovo utente da creare
     * @param user moderatore autenticato che esegue l'operazione
     * @return il {@link UserResponse} dell'utente appena creato
     */
    @Operation(summary = "Crea un nuovo utente")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.createUser(body, user));
    }

    /**
     * Aggiorna i dati di un utente esistente.
     *
     * @param id   ID dell'utente da aggiornare
     * @param body nuovi dati da applicare all'utente
     * @param user moderatore autenticato che esegue l'operazione
     * @return il {@link UserResponse} aggiornato
     */
    @Operation(summary = "Aggiorna un utente esistente")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body,@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.updateUser(id, body,user));
    }

    /**
     * Disabilita (soft delete) l'account di un utente impostando il flag {@code deleted=true}.
     * L'utente non potrà più autenticarsi ma i dati vengono conservati.
     *
     * @param id   ID dell'utente da disabilitare
     * @param user moderatore autenticato che esegue l'operazione
     * @return messaggio di conferma
     */
    @Operation(summary = "Disabilita un utente (soft delete)", description = "Disabilita l'account impostando deleted=true. L'utente non può più accedere.")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User user) {
        moderatorFacade.deleteUser(id, user);
        return ResponseEntity.ok(Map.of("message", "Utente disabilitato"));
    }

    /**
     * Aggiorna manualmente i crediti di un abbonamento (crediti PT e crediti nutrizionista).
     *
     * @param id   ID dell'abbonamento da aggiornare
     * @param body nuovi valori per creditsPT e creditsNutri
     * @return il {@link SubscriptionResponse} aggiornato
     */
    @Operation(summary = "Aggiorna i crediti di un abbonamento")
    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody UpdateCreditsRequest body) {
        return ResponseEntity.ok(moderatorFacade.updateSubscriptionCredits(
                id, body.creditsPT(), body.creditsNutri()));
    }

}
