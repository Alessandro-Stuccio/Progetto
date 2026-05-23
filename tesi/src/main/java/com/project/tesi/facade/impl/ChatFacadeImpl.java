package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.mapper.ChatMapper;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatFacadeImpl implements ChatFacade {

    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final UserService userService;

    public ChatFacadeImpl(ChatService chatService, ChatMapper chatMapper, UserService userService) {
        this.chatService = chatService;
        this.chatMapper = chatMapper;
        this.userService = userService;
    }

    @Override
    public Long createChat(Long senderId, Long receiverId) {
        return chatService.createChat(senderId, receiverId);
    }

    @Override
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        Message message = chatService.sendMessage(request, senderId);
        Long receiverId = chatService.getReceiverId(message.getChat(), senderId);
        return chatMapper.toMessageResponse(message, receiverId);
    }

    @Override
    public List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size) {
        List<Message> messages = chatService.getConversation(chatId, userId, page, size);
        Chat chat = chatService.getChatEntity(chatId);
        Long receiverId = chatService.getReceiverId(chat, userId);
        return chatMapper.toMessageResponseList(messages, receiverId);
    }

    @Override
    public List<ConversationPreviewResponse> getUserConversations(Long userId) {
        List<Chat> chats = chatService.getUserConversations(userId);
        User currentUser = userService.getUserById(userId);

        return chats.stream()
                .map(chat -> {
                    Message lastMsg = chatService.getLastMessage(chat.getId());
                    int unreadCount = chatService.getUnreadCount(chat.getId(), userId);
                    return chatMapper.toConversationPreview(chat, userId, lastMsg, unreadCount);
                })
                .filter(res -> {
                    if (res.getLastMessageTime() == null) {
                        Role role = currentUser.getRole();
                        if (role == Role.CLIENT || role == Role.PERSONAL_TRAINER || role == Role.NUTRITIONIST) {
                            if ("ADMIN".equals(res.getOtherUserRole()) || "MODERATOR".equals(res.getOtherUserRole())) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
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
