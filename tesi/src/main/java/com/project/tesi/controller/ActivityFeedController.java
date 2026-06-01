package com.project.tesi.controller;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.facade.ActivityFeedFacade;
import com.project.tesi.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Feed delle attività recenti dell'utente. Espone GET /api/activity/feed. */
@RestController
@RequestMapping("/api/activity")
public class ActivityFeedController {

    private final ActivityFeedFacade activityFeedFacade;

    public ActivityFeedController(ActivityFeedFacade activityFeedFacade) {
        this.activityFeedFacade = activityFeedFacade;
    }

    /** Prenotazioni e documenti degli ultimi {@code days} giorni, dal più recente. */
    @GetMapping("/feed")
    public ResponseEntity<List<ActivityFeedItemResponse>> getActivityFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(activityFeedFacade.getActivityFeed(user.getId(), days, size));
    }
}
