package com.project.tesi.service.impl;

import com.project.tesi.enums.ChatStatus;
import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatAsyncService;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.MessageService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementazione di ChatAsyncService. Esegue operazioni di chat in modo asincrono
 * usando il thread pool 'emailTaskExecutor'.
 */
@Service
public class ChatAsyncServiceImpl implements ChatAsyncService {

    private static final Logger log = LoggerFactory.getLogger(ChatAsyncServiceImpl.class);
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatAsyncServiceImpl(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    /**
     * Recupera la chat e l'utente mittente, verifica che la chat sia aperta
     * e che il mittente ne faccia parte, poi delega la persistenza a MessageService.
     */
    @Override
    public void saveChatMessage(Long chatId, Long senderId, String content) {
        doSave(chatId, senderId, content);
    }

    /**
     * Delega a MessageService la marcatura dei messaggi come consegnati,
     * eseguendo l'operazione in modo asincrono sul thread pool 'emailTaskExecutor'.
     */
    @Override
    @Async("emailTaskExecutor")
    @Transactional
    public void markAsDeliveredAsync(Long chatId, Long userId) {
        try {
            messageService.markAsDelivered(chatId, userId);
        } catch (Exception e) {
            log.error("[WS] MarkAsDelivered error chatId={} userId={}: {}", chatId, userId, e.getMessage(), e);
        }
    }

    /**
     * Delega a MessageService la marcatura dei messaggi come letti,
     * eseguendo l'operazione in modo asincrono sul thread pool 'emailTaskExecutor'.
     */
    @Override
    @Async("emailTaskExecutor")
    @Transactional
    public void markAsReadAsync(Long chatId, Long userId) {
        try {
            messageService.markAsRead(chatId, userId);
        } catch (Exception e) {
            log.error("[WS] MarkAsRead error chatId={} userId={}: {}", chatId, userId, e.getMessage(), e);
        }
    }

    private void doSave(Long chatId, Long senderId, String content) {
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            log.warn("[Chat] doSave: chat {} non trovata.", chatId);
            return;
        }
        if (chat.getStatus() == ChatStatus.CLOSED) {
            log.warn("[Chat] doSave: chat {} è CLOSED, save annullato.", chatId);
            return;
        }
        User sender = userService.getUserById(senderId);
        if (!chat.getUser1().getId().equals(senderId) && !chat.getUser2().getId().equals(senderId)) {
            log.warn("[Chat] doSave: utente {} non è parte della chat {}.", senderId, chatId);
            return;
        }
        messageService.saveMessage(chat, sender, content);
        log.info("[Chat] Messaggio persistito chatId={} senderId={}", chatId, senderId);
    }
}
