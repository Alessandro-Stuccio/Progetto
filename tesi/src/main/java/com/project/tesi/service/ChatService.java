package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione delle chat tra utenti, con qualche utility per i moderatori. */
@Validated
public interface ChatService {

    /** Ritorna l'id della chat tra i due utenti, creandola al volo se non esiste. */
    Long getOrCreateChat(@NotNull User sender, @NotNull User receiver);

    /** Tutte le conversazioni a cui partecipa l'utente. */
    List<Chat> getUserConversations(@NotNull @Min(1) Long userId);

    Chat getChatEntity(@NotNull @Min(1) Long chatId);

    Chat save(@NotNull Chat chat);

    /** Quante chat ancora aperte sono in carico a un moderatore. */
    long countOpenChatsByModerator(@NotNull @Min(1) Long moderatorId);

    /** Chiude la chat: da qui in poi non si possono più inviare messaggi. */
    void closeChat(@NotNull @Min(1) Long chatId, @NotNull User moderator);
}
