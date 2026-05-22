package com.project.tesi.controller;

import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.facade.AdminFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insurance")
@Tag(name = "Insurance", description = "API riservate all'Insurance Manager")
public class InsuranceController {

    private final AdminFacade adminFacade;

    public InsuranceController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @Operation(summary = "Lista abbonamenti", description = "Restituisce tutti gli abbonamenti attivi e scaduti.")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(adminFacade.getAllSubscriptions());
    }

    @Operation(summary = "Lista utenti", description = "Restituisce tutti gli utenti registrati.")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminFacade.getAllUsers());
    }
}
