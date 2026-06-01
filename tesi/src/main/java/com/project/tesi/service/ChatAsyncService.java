package com.project.tesi.service;

/**
 * Operazioni sulla chat eseguite in modo asincrono, così da non bloccare
 * il thread che gestisce la richiesta o il messaggio in arrivo.
 */
public interface ChatAsyncService {

    /** Salva un messaggio nella chat fuori dal thread chiamante. */
    void saveChatMessage(Long chatId, Long senderId, String content);

    /** Segna come consegnati i messaggi della chat per il destinatario. */
    void markAsDeliveredAsync(Long chatId, Long userId);

    /** Segna come letti i messaggi della chat per l'utente. */
    void markAsReadAsync(Long chatId, Long userId);
}
