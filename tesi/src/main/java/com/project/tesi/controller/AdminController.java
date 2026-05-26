package com.project.tesi.controller;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.SubscriptionCreditsUpdateDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.AdminFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "API di amministrazione per utenti, abbonamenti e piani")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @Operation(summary = "Recupera tutti gli utenti registrati")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminFacade.getAllUsers());
    }

    @Operation(summary = "Crea un nuovo utente")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody ModeratorUserUpdateRequest request) {
        return ResponseEntity.ok(adminFacade.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        adminFacade.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(adminFacade.getAllSubscriptions());
    }

    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id, @Valid @RequestBody SubscriptionCreditsUpdateDTO request) {
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
    public ResponseEntity<PlanResponseDTO> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.updatePlan(id, request));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        adminFacade.deletePlan(id);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }
}
