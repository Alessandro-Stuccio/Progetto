package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Servizio per la gestione delle chat tra utenti.
 * Espone operazioni CRUD sulla chat e utility per moderatori.
 */
@Validated
public interface ChatService {

    /**
     * Restituisce l'identificativo della chat esistente tra mittente e destinatario,
     * creandola se non esiste ancora.
     *
     * @param sender   utente mittente
     * @param receiver utente destinatario
     * @return identificativo della chat
     */
    Long getOrCreateChat(@NotNull User sender, @NotNull User receiver);

    /**
     * Restituisce tutte le conversazioni a cui partecipa un utente.
     *
     * @param userId identificativo dell'utente
     * @return lista delle chat dell'utente
     */
    List<Chat> getUserConversations(@NotNull @Min(1) Long userId);

    /**
     * Recupera l'entità {@link Chat} tramite il suo identificativo.
     *
     * @param chatId identificativo della chat
     * @return entità chat corrispondente
     */
    Chat getChatEntity(@NotNull @Min(1) Long chatId);

    /**
     * Persiste l'entità chat nel database.
     *
     * @param chat entità da salvare
     * @return entità salvata aggiornata
     */
    Chat save(@NotNull Chat chat);

    /**
     * Conta le chat aperte assegnate a un moderatore specifico.
     *
     * @param moderatorId identificativo del moderatore
     * @return numero di chat aperte gestite dal moderatore
     */
    long countOpenChatsByModerator(@NotNull @Min(1) Long moderatorId);

    /**
     * Chiude una chat, impedendo ulteriori messaggi.
     *
     * @param chatId    identificativo della chat da chiudere
     * @param moderator utente moderatore che esegue la chiusura
     */
    void closeChat(@NotNull @Min(1) Long chatId, @NotNull User moderator);
}
