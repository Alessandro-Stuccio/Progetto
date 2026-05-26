package com.project.tesi.service.impl;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.enums.ChatStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.chat.ChatNotAllowedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.repository.MessageRepository;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public ChatServiceImpl(ChatRepository chatRepository,
                           MessageRepository messageRepository,
                           UserService userService) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

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
    public Message sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat", request.chatId()));

        if (chat.getStatus() == ChatStatus.CLOSED) {
            throw new ChatNotAllowedException("La chat è chiusa e non accetta nuovi messaggi.");
        }

        User sender = userService.getUserById(senderId);

        if (!chat.getUser1().getId().equals(sender.getId()) && !chat.getUser2().getId().equals(sender.getId())) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }

        return buildAndSaveMessage(chat, sender, request.content());
    }

    /**
     * Versione fire-and-forget di sendMessage: usata da ChatAsyncService e ChatMessageConsumer
     * in contesti asincroni dove un'eccezione non sarebbe gestita. Usa orElse(null) invece di
     * orElseThrow per fallire silenziosamente con log warning invece di propagare eccezioni.
     */
    @Override
    @Transactional
    public void sendMessageDirect(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        User sender = userService.findById(senderId).orElse(null);
        if (chat == null) {
            log.warn("[Chat] sendMessageDirect: chat {} non trovata.", chatId);
            return;
        }
        if (sender == null) {
            log.warn("[Chat] sendMessageDirect: sender {} non trovato.", senderId);
            return;
        }
        if (chat.getStatus() == ChatStatus.CLOSED) {
            log.warn("[Chat] sendMessageDirect: chat {} è CLOSED, save annullato.", chatId);
            return;
        }
        if (!chat.getUser1().getId().equals(sender.getId()) && !chat.getUser2().getId().equals(sender.getId())) {
            log.warn("[Chat] sendMessageDirect: utente {} non è parte della chat {}.", senderId, chatId);
            return;
        }

        Message saved = buildAndSaveMessage(chat, sender, content);
        log.info("[Chat] Messaggio persistito id={} chatId={} senderId={}", saved.getId(), chatId, senderId);
    }

    private Message buildAndSaveMessage(Chat chat, User sender, String content) {
        boolean sentByUser1 = chat.getUser1().getId().equals(sender.getId());
        Message message = Message.builder()
                .chat(chat)
                .sentByUser1(sentByUser1)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .isRead(false)
                .build();
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getConversation(Long chatId, Long userId, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));

        if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }

        return messageRepository.findMessagesByChatId(chat.getId(), PageRequest.of(page, size));
    }

    @Override
    public List<Chat> getUserConversations(Long userId) {
        return chatRepository.findAllChatsByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long chatId, Long userId) {
        chatRepository.findById(chatId).ifPresent(chat -> {
            if (chat.getUser1().getId().equals(userId) || chat.getUser2().getId().equals(userId)) {
                messageRepository.markMessagesAsRead(chat.getId(), userId);
            }
        });
    }

    @Override
    public int getTotalUnreadCount(Long userId) {
        return messageRepository.countTotalUnreadMessagesByUserId(userId);
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
    public void deleteChatsForUser(Long userId) {
        List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
        chatRepository.deleteAll(chats);
    }

    @Override
    public void deleteChatByUser(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        boolean isParticipant = chat.getUser1().getId().equals(userId)
                || chat.getUser2().getId().equals(userId);
        if (!isParticipant) {
            throw new UnauthorizedAccessException("Non sei partecipante di questa chat.");
        }
        chatRepository.delete(chat);
    }

    @Override
    public Message getLastMessage(Long chatId) {
        return messageRepository.findLastMessageByChatId(chatId);
    }

    @Override
    public int getUnreadCount(Long chatId, Long userId) {
        return messageRepository.countUnreadMessagesByChatIdAndUserId(chatId, userId);
    }

    @Override
    public long countOpenChatsByModerator(Long moderatorId) {
        return chatRepository.countOpenChatsByModerator(moderatorId);
    }

    @Override
    public void closeChat(Long chatId, Long moderatorId) {
        User moderator = userService.getUserById(moderatorId);
        if (moderator.getRole() != Role.MODERATOR && moderator.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo i moderatori possono chiudere le chat");
        }
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        chat.setClosedBy(moderator);
        chatRepository.save(chat);
    }
}
