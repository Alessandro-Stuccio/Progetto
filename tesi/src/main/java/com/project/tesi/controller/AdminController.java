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

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "API di amministrazione per utenti, abbonamenti e piani")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @Operation(summary = "Recupera tutti gli utenti gestibili")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getManageableUsers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.getManageableUsers(user));
    }

    @Operation(summary = "Crea un nuovo utente")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.createUser(body, user));
    }

    @Operation(summary = "Aggiorna un utente esistente")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminFacade.updateUser(id, body, user));
    }

    @Operation(summary = "Disabilita un utente (soft delete)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        adminFacade.deleteUser(id, user);
        return ResponseEntity.ok(Map.of("message", "Utente disabilitato"));
    }

    @Operation(summary = "Recupera i contatti per la chat")
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(adminFacade.getChatContacts());
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(adminFacade.getAllSubscriptions());
    }

    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody SubscriptionCreditsUpdateDTO request) {
        return ResponseEntity.ok(adminFacade.updateSubscriptionCredits(
                id,
                request.creditsPT() != null ? request.creditsPT() : 0,
                request.creditsNutri() != null ? request.creditsNutri() : 0
        ));
    }

    @PostMapping("/plans")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.createPlan(request));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@PathVariable Long id,
            @Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.updatePlan(id, request));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        adminFacade.deletePlan(id);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }

    @Operation(summary = "Statistiche aggregate per la dashboard admin")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminFacade.getAdminStats());
    }
}
