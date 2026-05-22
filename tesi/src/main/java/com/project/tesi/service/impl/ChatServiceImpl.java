package com.project.tesi.service.impl;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.enums.ChatStatus;
import com.project.tesi.enums.MessageStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.chat.ChatNotAllowedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.repository.MessageRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatServiceImpl(ChatRepository chatRepository,
                           MessageRepository messageRepository,
                           UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Long createChat(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Non puoi avviare una chat con te stesso");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Mittente", senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario", receiverId));

        validateChatPermission(sender, receiver);

        Chat chat = getOrCreateChat(sender, receiver);
        return chat.getId();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat", request.chatId()));

        if (chat.getStatus() == ChatStatus.CLOSED) {
            throw new ChatNotAllowedException("La chat è chiusa e non accetta nuovi messaggi.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Mittente", senderId));

        if (!chat.getUser1().getId().equals(sender.getId()) && !chat.getUser2().getId().equals(sender.getId())) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }

        boolean sentByUser1 = chat.getUser1().getId().equals(sender.getId());

        Message message = Message.builder()
                .chat(chat)
                .sentByUser1(sentByUser1)
                .content(request.content())
                .timeStamp(LocalDateTime.now())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        Long receiverId = sentByUser1 ? chat.getUser2().getId() : chat.getUser1().getId();
        return toChatMessageResponse(saved, receiverId);
    }

    @Override
    @Transactional
    public void sendMessageDirect(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        User sender = userRepository.findById(senderId).orElse(null);
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

        boolean sentByUser1 = chat.getUser1().getId().equals(sender.getId());

        Message message = Message.builder()
                .chat(chat)
                .sentByUser1(sentByUser1)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        log.info("[Chat] Messaggio persistito id={} chatId={} senderId={}", saved.getId(), chatId, senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));

        if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }

        List<Message> messages = messageRepository.findMessagesByChatId(chat.getId(), PageRequest.of(page, size));

        return messages.stream()
                .map(m -> {
                    Long receiverId = m.isSentByUser1() ? chat.getUser2().getId() : chat.getUser1().getId();
                    return toChatMessageResponse(m, receiverId);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationPreviewResponse> getUserConversations(Long userId) {
        List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
        User currentUser = userRepository.findById(userId).orElse(null);

        return chats.stream().map(chat -> {
            User partner = chat.getUser1().getId().equals(userId) ? chat.getUser2() : chat.getUser1();
            Message lastMsg = messageRepository.findLastMessageByChatId(chat.getId());
            int unreadCount = messageRepository.countUnreadMessagesByChatIdAndUserId(chat.getId(), userId);

            return ConversationPreviewResponse.builder()
                    .chatId(chat.getId())
                    .otherUserId(partner.getId())
                    .otherUserName(partner.getFullName())
                    .otherUserRole(partner.getRole() != null ? partner.getRole().name() : null)
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                    .lastMessageTime(lastMsg != null ? lastMsg.getTimeStamp() : null)
                    .unreadCount(unreadCount)
                    .terminated(chat.getStatus() == ChatStatus.CLOSED)
                    .build();
        })
        .filter(res -> {
            if (res.getLastMessageTime() == null && currentUser != null) {
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
        chatRepository.findById(chatId).ifPresent(chat -> {
            if (chat.getUser1().getId().equals(userId) || chat.getUser2().getId().equals(userId)) {
                messageRepository.markMessagesAsRead(chat.getId(), userId);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalUnreadCount(Long userId) {
        return messageRepository.countTotalUnreadMessagesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserFullName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse("Utente");
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatEntity(Long chatId) {
        return chatRepository.findById(chatId).orElse(null);
    }

    @Override
    @Transactional
    public void closeChat(Long chatId, Long moderatorId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderatore", moderatorId));
        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        chat.setClosedBy(moderator);
        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void closeChatByUser(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
        boolean isParticipant = chat.getUser1().getId().equals(userId)
                || chat.getUser2().getId().equals(userId);
        if (!isParticipant) {
            throw new UnauthorizedAccessException("Non sei partecipante di questa chat.");
        }
        chatRepository.delete(chat); // Elimina chat e messaggi (cascade); alla prossima apertura si crea una chat nuova
    }

    private Chat getOrCreateChat(User user1, User user2) {
        return chatRepository.findChatBetweenUsers(user1.getId(), user2.getId())
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .user1(user1)
                            .user2(user2)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRepository.save(newChat);
                });
    }

    private void validateChatPermission(User uA, User uB) {
        // ADMIN può chattare con tutti (è l'unico, non può duplicarsi)
        if (uA.getRole() == Role.ADMIN || uB.getRole() == Role.ADMIN) return;

        // INSURANCE_MANAGER può contattare solo ADMIN (già gestito sopra) — blocca tutto il resto
        if (uA.getRole() == Role.INSURANCE_MANAGER || uB.getRole() == Role.INSURANCE_MANAGER) {
            throw new ChatNotAllowedException("Insurance manager può contattare solo l'amministratore.");
        }

        // MODERATOR può chattare con tutti tranne INSURANCE_MANAGER (già bloccato sopra)
        if (uA.getRole() == Role.MODERATOR || uB.getRole() == Role.MODERATOR) return;

        boolean professionalAssigned = false;
        User client = null;
        User prof = null;
        if (uA.getRole() == Role.CLIENT) { client = uA; prof = uB; }
        else if (uB.getRole() == Role.CLIENT) { client = uB; prof = uA; }

        if (client != null && prof != null) {
            if (prof.getRole() == Role.PERSONAL_TRAINER && client.getAssignedPT() != null
                    && client.getAssignedPT().getId().equals(prof.getId())) {
                professionalAssigned = true;
            }
            if (prof.getRole() == Role.NUTRITIONIST && client.getAssignedNutritionist() != null
                    && client.getAssignedNutritionist().getId().equals(prof.getId())) {
                professionalAssigned = true;
            }
        }

        if (!professionalAssigned) {
            throw new ChatNotAllowedException("Non sei assegnato a questo utente");
        }
    }

    private ChatMessageResponse toChatMessageResponse(Message m, Long receiverId) {
        User sender = m.isSentByUser1() ? m.getChat().getUser1() : m.getChat().getUser2();
        return ChatMessageResponse.builder()
                .id(m.getId())
                .chatId(m.getChat().getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiverId)
                .content(m.getContent())
                .createdAt(m.getTimeStamp())
                .status(m.isRead() ? MessageStatus.READ : MessageStatus.SENT)
                .build();
    }
}
