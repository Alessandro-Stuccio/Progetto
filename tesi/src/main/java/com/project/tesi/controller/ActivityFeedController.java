package com.project.tesi.controller;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.facade.ActivityFeedFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST per il feed di attività recente.
 * Espone GET /api/activity/feed.
 */
@RestController
@RequestMapping("/api/activity")
@Tag(name = "Activity Feed", description = "Feed delle attività recenti dell'utente autenticato")
public class ActivityFeedController {

    private final ActivityFeedFacade activityFeedFacade;

    public ActivityFeedController(ActivityFeedFacade activityFeedFacade) {
        this.activityFeedFacade = activityFeedFacade;
    }

    /**
     * Recupera il feed di attività (prenotazioni, documenti) degli ultimi N giorni
     * per l'utente autenticato.
     *
     * @param user  utente autenticato ricavato dal token JWT
     * @param days  numero di giorni passati da considerare (default 14)
     * @param size  numero massimo di elementi da restituire (default 15)
     * @return lista di {@link ActivityFeedItemResponse} ordinata dal più recente
     */
    @Operation(summary = "Feed attività recenti", description = "Restituisce prenotazioni e documenti degli ultimi N giorni, ordinati dal più recente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feed restituito con successo"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido")
    })
    @GetMapping("/feed")
    public ResponseEntity<List<ActivityFeedItemResponse>> getActivityFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(activityFeedFacade.getActivityFeed(user.getId(), days, size));
    }
}
