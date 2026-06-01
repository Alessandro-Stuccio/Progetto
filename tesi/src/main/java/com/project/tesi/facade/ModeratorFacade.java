package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.model.User;

import java.util.List;

/**
 * Operazioni del Moderatore: gestione utenti, abbonamenti e chat.
 */
public interface ModeratorFacade {

    /**
     * Gli utenti che questo moderatore ha il diritto di gestire.
     */
    List<UserResponse> getManageableUsers(User user);

    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Utenti che il moderatore può contattare in chat.
     */
    List<UserResponse> getChatContacts();

    /**
     * Crea un utente per conto del moderatore.
     */
    UserResponse createUser(UserCreateRequestDTO request, User user);

    /**
     * Aggiorna l'utente indicato, verificando i permessi del moderatore.
     */
    UserResponse updateUser(Long id, ModeratorUserUpdateRequest request, User user);

    /**
     * Elimina l'utente indicato, verificando i permessi del moderatore.
     */
    void deleteUser(Long id, User user);

    /**
     * Imposta i crediti mensili dell'abbonamento per personal trainer e nutrizionista.
     */
    SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri);
}
