package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.enums.ChatStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.chat.ChatNotAllowedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.mapper.ChatMapper;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Transactional
    public Long createChat(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Non puoi avviare una chat con te stesso");
        }
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        validateChatPermission(sender, receiver);
        return chatService.getOrCreateChat(sender, receiver);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        Message message = chatService.sendMessage(request, senderId);
        Long receiverId = getReceiverId(message.getChat(), senderId);
        return chatMapper.toMessageResponse(message, receiverId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size) {
        List<Message> messages = chatService.getConversation(chatId, userId, page, size);
        Chat chat = chatService.getChatEntity(chatId);
        Long receiverId = getReceiverId(chat, userId);
        return chatMapper.toMessageResponseList(messages, receiverId);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
    public void markAsRead(Long chatId, Long userId) {
        chatService.markAsRead(chatId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalUnreadCount(Long userId) {
        return chatService.getTotalUnreadCount(userId);
    }

    @Override
    @Transactional
    public void closeChat(Long chatId, Long moderatorId) {
        User moderator = userService.getUserById(moderatorId);
        if (moderator.getRole() != Role.MODERATOR && moderator.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo i moderatori possono chiudere le chat");
        }
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            throw new ResourceNotFoundException("Chat", chatId);
        }
        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        chat.setClosedBy(moderator);
        chatService.save(chat);
    }

    @Override
    @Transactional
    public void deleteChatByUser(Long chatId, Long userId) {
        chatService.deleteChatByUser(chatId, userId);
    }

    private Long getReceiverId(Chat chat, Long currentUserId) {
        return chat.getUser1().getId().equals(currentUserId)
                ? chat.getUser2().getId()
                : chat.getUser1().getId();
    }

    private void validateChatPermission(User uA, User uB) {
        if (uA.getRole() == Role.ADMIN || uB.getRole() == Role.ADMIN) return;

        if (uA.getRole() == Role.INSURANCE_MANAGER || uB.getRole() == Role.INSURANCE_MANAGER) {
            throw new ChatNotAllowedException("Insurance manager può contattare solo l'amministratore.");
        }

        if (uA.getRole() == Role.MODERATOR || uB.getRole() == Role.MODERATOR) return;

        User client = null, prof = null;
        if (uA.getRole() == Role.CLIENT) { client = uA; prof = uB; }
        else if (uB.getRole() == Role.CLIENT) { client = uB; prof = uA; }

        boolean assigned = false;
        if (client != null && prof != null) {
            if (prof.getRole() == Role.PERSONAL_TRAINER && client.getAssignedPT() != null
                    && client.getAssignedPT().getId().equals(prof.getId())) assigned = true;
            if (prof.getRole() == Role.NUTRITIONIST && client.getAssignedNutritionist() != null
                    && client.getAssignedNutritionist().getId().equals(prof.getId())) assigned = true;
        }
        if (!assigned) throw new ChatNotAllowedException("Non sei assegnato a questo utente");
    }
}
