package com.project.kore.facade;

import com.project.kore.dto.response.ActivityFeedItemResponse;

import java.util.List;

/**
 * Recupera il feed delle attività recenti di un utente.
 */
public interface ActivityFeedFacade {

    /**
     * Restituisce le attività dell'utente negli ultimi {@code days} giorni, al massimo {@code limit} elementi.
     *
     * @param userId id dell'utente
     * @param days   ampiezza della finestra temporale, in giorni
     * @param limit  numero massimo di elementi da restituire
     * @return le attività recenti dell'utente
     */
    List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit);
}
