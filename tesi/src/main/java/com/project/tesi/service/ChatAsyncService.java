package com.project.tesi.service;

/**
 * Servizio per operazioni asincrone sulla chat.
 * Gestisce salvataggio messaggi e aggiornamento stato consegna/lettura in modo non bloccante.
 */
public interface ChatAsyncService {

    /**
     * Salva in modo asincrono un messaggio all'interno di una chat.
     *
     * @param chatId   identificativo della chat
     * @param senderId identificativo del mittente
     * @param content  contenuto testuale del messaggio
     */
    void saveChatMessage(Long chatId, Long senderId, String content);

    /**
     * Marca in modo asincrono i messaggi di una chat come consegnati per un utente.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente destinatario
     */
    void markAsDeliveredAsync(Long chatId, Long userId);

    /**
     * Marca in modo asincrono i messaggi di una chat come letti da un utente.
     *
     * @param chatId identificativo della chat
     * @param userId identificativo dell'utente lettore
     */
    void markAsReadAsync(Long chatId, Long userId);
}
