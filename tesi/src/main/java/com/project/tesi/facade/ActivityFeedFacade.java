package com.project.tesi.facade;

import com.project.tesi.dto.response.ActivityFeedItemResponse;

import java.util.List;

/**
 * Facade per il recupero del feed di attività recenti di un utente.
 */
public interface ActivityFeedFacade {

    /**
     * Restituisce il feed delle attività recenti dell'utente specificato.
     *
     * @param userId identificativo dell'utente di cui recuperare il feed
     * @param days   numero di giorni passati da considerare per il feed
     * @param limit  numero massimo di elementi da restituire
     * @return lista di attività recenti dell'utente
     */
    List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit);
}
