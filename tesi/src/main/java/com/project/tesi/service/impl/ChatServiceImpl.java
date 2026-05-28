package com.project.tesi.service.impl;

import com.project.tesi.enums.ChatStatus;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.service.ChatService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementazione di ChatService. Gestisce la creazione e recupero delle chat
 * tramite ChatRepository.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /**
     * Cerca una chat esistente tra i due utenti tramite repository;
     * se non trovata ne crea una nuova con stato predefinito e la persiste.
     */
    @Override
    public Long getOrCreateChat(User sender, User receiver) {
        return chatRepository.findChatBetweenUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .user1(sender)
                            .user2(receiver)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRepository.save(newChat);
                })
                .getId();
    }

    @Override
    public List<Chat> getUserConversations(Long userId) {
        return chatRepository.findAllChatsByUserId(userId);
    }

    @Override
    public Chat getChatEntity(Long chatId) {
        return chatRepository.findById(chatId).orElse(null);
    }

    @Override
    public Chat save(Chat chat) {
        return chatRepository.save(chat);
    }

    @Override
    public long countOpenChatsByModerator(Long moderatorId) {
        return chatRepository.countOpenChatsByModerator(moderatorId);
    }

    @Override
    public void closeChat(Long chatId, User moderator) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        chat.setClosedBy(moderator);
        chatRepository.save(chat);
    }
}
