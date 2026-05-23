package com.project.tesi.controller;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint REST per le recensioni. Permette ai clienti di valutare i professionisti post-appuntamento.
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Recensioni dei clienti verso i professionisti")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final UserFacade userFacade;

    public ReviewController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Operation(summary = "Aggiungi recensione", description = "Il cliente lascia una recensione (1-5 stelle) a un professionista con cui ha avuto almeno un appuntamento.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recensione aggiunta"),
        @ApiResponse(responseCode = "400", description = "Recensione già presente o nessun appuntamento effettuato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@RequestBody ReviewRequest request,
                                                     @AuthenticationPrincipal User user) {
        log.info("Aggiunta recensione per professionista {} da utente {}", request.professionalId(), user.getId());
        return ResponseEntity.ok(userFacade.addReview(request, user.getId()));
    }

    @Operation(summary = "Recensioni professionista", description = "Restituisce tutte le recensioni ricevute dal professionista specificato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista recensioni"),
        @ApiResponse(responseCode = "404", description = "Professionista non trovato")
    })
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForProfessional(@PathVariable Long professionalId) {
        return ResponseEntity.ok(userFacade.getReviewsForProfessional(professionalId));
    }

    @Operation(summary = "Verifica possibilità di recensire", description = "Indica se il cliente può ancora recensire il professionista (canReview) e se lo ha già fatto (hasReviewed).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verifica completata"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping("/can-review")
    public ResponseEntity<Map<String, Object>> canReview(@AuthenticationPrincipal User user,
                                                          @RequestParam Long professionalId) {
        boolean hasReviewed = userFacade.hasClientReviewed(user.getId(), professionalId);
        boolean can = !hasReviewed && userFacade.canClientReview(user.getId(), professionalId);
        return ResponseEntity.ok(Map.of("canReview", can, "hasReviewed", hasReviewed));
    }
}
