package com.project.tesi.controller;

import com.project.tesi.dto.request.UpdateCreditsRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.ModeratorFacade;
import com.project.tesi.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<UserResponse>> getManageableUsers() {
        return ResponseEntity.ok(moderatorFacade.getManageableUsers());
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
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body) {
        return ResponseEntity.ok(moderatorFacade.createUser(body));
    }

    @Operation(summary = "Aggiorna un utente esistente")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body) {
        return ResponseEntity.ok(moderatorFacade.updateUser(id, body));
    }

    @Operation(summary = "Elimina un utente")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        moderatorFacade.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Utente eliminato"));
    }

    @Operation(summary = "Aggiorna i crediti di un abbonamento")
    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody UpdateCreditsRequest body) {
        return ResponseEntity.ok(moderatorFacade.updateSubscriptionCredits(
                id, body.creditsPT(), body.creditsNutri()));
    }

    @Operation(summary = "Chiude formalmente una sessione di chat")
    @PostMapping("/chat/{chatId}/close")
    public ResponseEntity<Void> closeChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User moderator) {
        moderatorFacade.closeChat(chatId, moderator.getId());
        return ResponseEntity.noContent().build();
    }
}
