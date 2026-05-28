package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.model.User;

import java.util.List;

/**
 * Facade per le operazioni del Moderatore: gestione utenti, abbonamenti e chat.
 */
public interface ModeratorFacade {

    /**
     * Restituisce la lista degli utenti gestibili dal moderatore specificato.
     *
     * @param user moderatore che effettua la richiesta
     * @return lista degli utenti gestibili
     */
    List<UserResponse> getManageableUsers(User user);

    /**
     * Restituisce la lista di tutti gli abbonamenti.
     *
     * @return lista di tutti gli abbonamenti
     */
    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Restituisce i contatti disponibili per la chat del moderatore.
     *
     * @return lista degli utenti contattabili
     */
    List<UserResponse> getChatContacts();

    /**
     * Crea un nuovo utente nel sistema.
     *
     * @param request dati del nuovo utente da creare
     * @param user    moderatore che esegue la creazione
     * @return informazioni dell'utente creato
     */
    UserResponse createUser(UserCreateRequestDTO request, User user);

    /**
     * Aggiorna i dati di un utente esistente.
     *
     * @param id      identificativo dell'utente da aggiornare
     * @param request nuovi dati dell'utente
     * @param user    moderatore che esegue l'aggiornamento
     * @return informazioni dell'utente aggiornato
     */
    UserResponse updateUser(Long id, ModeratorUserUpdateRequest request, User user);

    /**
     * Elimina un utente dal sistema.
     *
     * @param id   identificativo dell'utente da eliminare
     * @param user moderatore che esegue l'eliminazione
     */
    void deleteUser(Long id, User user);

    /**
     * Aggiorna i crediti di abbonamento di un utente per personal trainer e nutrizionista.
     *
     * @param id    identificativo dell'abbonamento da aggiornare
     * @param pt    nuovi crediti per il personal trainer
     * @param nutri nuovi crediti per il nutrizionista
     * @return abbonamento aggiornato
     */
    SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri);
}
