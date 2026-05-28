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

@RestController
@RequestMapping("/api/moderator")
@Tag(name = "Moderator", description = "API per i moderatori")
public class ModeratorController {

    private final ModeratorFacade moderatorFacade;

    public ModeratorController(ModeratorFacade moderatorFacade) {
        this.moderatorFacade = moderatorFacade;
    }

    @Operation(summary = "Recupera gli utenti gestibili")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getManageableUsers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.getManageableUsers(user));
    }

    @Operation(summary = "Recupera tutti gli abbonamenti")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(moderatorFacade.getAllSubscriptions());
    }

    @Operation(summary = "Recupera i contatti per la chat")
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(moderatorFacade.getChatContacts());
    }

    @Operation(summary = "Crea un nuovo utente")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.createUser(body, user));
    }

    @Operation(summary = "Aggiorna un utente esistente")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body,@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.updateUser(id, body,user));
    }

    @Operation(summary = "Disabilita un utente (soft delete)", description = "Disabilita l'account impostando deleted=true. L'utente non può più accedere.")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User user) {
        moderatorFacade.deleteUser(id, user);
        return ResponseEntity.ok(Map.of("message", "Utente disabilitato"));
    }

    @Operation(summary = "Aggiorna i crediti di un abbonamento")
    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody UpdateCreditsRequest body) {
        return ResponseEntity.ok(moderatorFacade.updateSubscriptionCredits(
                id, body.creditsPT(), body.creditsNutri()));
    }

}
