package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Servizio per la gestione dei messaggi all'interno di una chat.
 */
@Validated
public interface MessageService {

    /**
     * Crea e persiste un nuovo messaggio nella chat specificata.
     *
     * @param chat    entità chat di destinazione
     * @param sender  utente mittente
     * @param content testo del messaggio
     * @return messaggio salvato
     */
    Message saveMessage(@NotNull Chat chat, @NotNull User sender, @NotBlank String content);

    /**
     * Restituisce i messaggi di una chat in modo paginato.
     *
     * @param chatId identificativo della chat
     * @param page   numero di pagina (0-based)
     * @param size   dimensione della pagina
     * @return lista paginata dei messaggi
     */
    List<Message> getMessages(@NotNull @Min(1) Long chatId, @Min(0) int page, @Min(1) int size);

    /**
     * Marca tutti i messaggi non ancora consegnati in una chat come consegnati per un utente.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente destinatario
     */
    void markAsDelivered(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /**
     * Marca tutti i messaggi non ancora letti in una chat come letti da un utente.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente lettore
     */
    void markAsRead(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /**
     * Restituisce il numero totale di messaggi non letti per un utente su tutte le chat.
     *
     * @param userId identificativo dell'utente
     * @return conteggio totale dei messaggi non letti
     */
    int getTotalUnreadCount(@NotNull @Min(1) Long userId);

    /**
     * Recupera l'ultimo messaggio inviato in una chat.
     *
     * @param chatId identificativo della chat
     * @return ultimo messaggio della chat
     */
    Message getLastMessage(@NotNull @Min(1) Long chatId);

    /**
     * Restituisce il numero di messaggi non letti in una chat per un utente specifico.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente
     * @return numero di messaggi non letti
     */
    int getUnreadCount(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);
}
