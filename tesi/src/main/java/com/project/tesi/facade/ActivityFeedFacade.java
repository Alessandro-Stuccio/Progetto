package com.project.tesi.facade;

import com.project.tesi.dto.response.ActivityFeedItemResponse;

import java.util.List;

/**
 * Recupera il feed delle attività recenti di un utente.
 */
public interface ActivityFeedFacade {

    /**
     * Restituisce le attività dell'utente negli ultimi {@code days} giorni, al massimo {@code limit} elementi.
     */
    List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit);
}
