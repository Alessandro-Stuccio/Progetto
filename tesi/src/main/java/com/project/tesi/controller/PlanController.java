package com.project.tesi.controller;

import com.project.tesi.facade.PlanFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.Plan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per i piani di abbonamento (pubblico).
 */
@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plans", description = "Piani di abbonamento disponibili (endpoint pubblico)")
public class PlanController {

    private final PlanFacade planFacade;

    public PlanController(PlanFacade planFacade) {
        this.planFacade = planFacade;
    }

    /** Restituisce la lista di tutti i piani di abbonamento disponibili. */
    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planFacade.getAllPlans());
    }
}
