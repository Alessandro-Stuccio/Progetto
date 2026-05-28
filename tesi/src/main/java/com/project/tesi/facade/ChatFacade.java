package com.project.tesi.facade;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.model.User;

import java.util.List;

public interface ChatFacade {
    Long createChat(Long senderId, Long receiverId);
    ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId);
    List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size);
    List<ConversationPreviewResponse> getUserConversations(Long userId);
    void markAsRead(Long chatId, Long userId);
    Integer getTotalUnreadCount(Long userId);
    void closeChat(Long chatId, Long moderatorId);
    ClientBasicInfoResponse getModerator(User user);
}
