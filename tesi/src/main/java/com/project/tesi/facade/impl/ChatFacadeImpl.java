package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.service.ChatService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementazione del facade per le operazioni di chat.
 */
@Component
public class ChatFacadeImpl implements ChatFacade {

    private final ChatService chatService;

    public ChatFacadeImpl(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Long createChat(Long senderId, Long receiverId) {
        return chatService.createChat(senderId, receiverId);
    }

    @Override
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        return chatService.sendMessage(request, senderId);
    }

    @Override
    public List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size) {
        return chatService.getConversation(chatId, userId, page, size);
    }

    @Override
    public List<ConversationPreviewResponse> getUserConversations(Long userId) {
        return chatService.getUserConversations(userId);
    }

    @Override
    public void markAsRead(Long chatId, Long userId) {
        chatService.markAsRead(chatId, userId);
    }

    @Override
    public Integer getTotalUnreadCount(Long userId) {
        return chatService.getTotalUnreadCount(userId);
    }

    @Override
    public void closeChatByUser(Long chatId, Long userId) {
        chatService.closeChatByUser(chatId, userId);
    }
}
