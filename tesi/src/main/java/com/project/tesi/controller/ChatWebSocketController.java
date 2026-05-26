package com.project.tesi.controller;

import com.project.tesi.config.WebSocketEventListener;
import com.project.tesi.dto.request.ws.JoinRoomRequest;
import com.project.tesi.dto.request.ws.LeaveRoomRequest;
import com.project.tesi.dto.request.ws.WsMarkReadRequest;
import com.project.tesi.dto.request.ws.WsSendMessageRequest;
import com.project.tesi.dto.response.WsMessageResponse;
import com.project.tesi.dto.response.WsNotificationResponse;
import com.project.tesi.dto.response.WsUnreadUpdateResponse;
import com.project.tesi.messaging.ChatMessagePublisher;
import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatAsyncService;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketEventListener eventListener;
    private final ChatService chatService;
    private final ChatAsyncService chatAsyncService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final UserService userService;

    public ChatWebSocketController(SimpMessageSendingOperations messagingTemplate,
                                   WebSocketEventListener eventListener,
                                   ChatService chatService,
                                   ChatAsyncService chatAsyncService,
                                   ChatMessagePublisher chatMessagePublisher,
                                   UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.eventListener = eventListener;
        this.chatService = chatService;
        this.chatAsyncService = chatAsyncService;
        this.chatMessagePublisher = chatMessagePublisher;
        this.userService = userService;
    }

    @MessageMapping("/chat.join")
    public void joinRoom(@Payload JoinRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            eventListener.joinRoom(sid, request.roomId());
        }
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload LeaveRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            eventListener.leaveRoom(sid, request.roomId());
        }
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload WsSendMessageRequest request, Principal principal) {
        User sender = extractUser(principal);
        if (sender == null) {
            log.warn("[WS] /chat.send rifiutato: Principal mancante o non valido.");
            return;
        }

        Long senderId = sender.getId();
        Long chatId = request.chatId();
        String content = request.content();
        String roomId = String.valueOf(chatId);

        log.info("[WS] /chat.send ricevuto: senderId={}, chatId={}, contentLen={}",
                senderId, chatId, content != null ? content.length() : 0);

        WsMessageResponse msg = WsMessageResponse.builder()
                .id(System.currentTimeMillis())
                .senderId(senderId)
                .senderName(userService.getUserById(senderId).getFullName())
                .chatId(chatId)
                .content(content)
                .status("SENT")
                .createdAt(LocalDateTime.now().toString())
                .roomId(roomId)
                .build();

        Long receiverId = null;
        String receiverEmail = null;
        try {
            Chat chat = chatService.getChatEntity(chatId);
            if (chat == null) {
                log.warn("[WS] /chat.send: chat {} non trovata.", chatId);
            } else {
                User receiver = chat.getUser1().getId().equals(senderId)
                        ? chat.getUser2()
                        : chat.getUser1();
                receiverId = receiver.getId();
                receiverEmail = receiver.getEmail();
                msg = WsMessageResponse.builder()
                        .id(msg.getId())
                        .senderId(msg.getSenderId())
                        .senderName(msg.getSenderName())
                        .chatId(msg.getChatId())
                        .content(msg.getContent())
                        .status(msg.getStatus())
                        .createdAt(msg.getCreatedAt())
                        .roomId(msg.getRoomId())
                        .receiverId(receiverId)
                        .receiverName(userService.getUserById(receiverId).getFullName())
                        .build();
            }
        } catch (Exception e) {
            log.warn("[WS] /chat.send: errore nel recupero chat {} — {}", chatId, e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, msg);

        chatMessagePublisher.publish(chatId, senderId, content);

        if (receiverEmail != null) {
            try {
                messagingTemplate.convertAndSendToUser(
                        receiverEmail, "/queue/notifications",
                        new WsNotificationResponse("NEW_MESSAGE", msg));
            } catch (Exception e) {
                log.warn("[WS] notifica NEW_MESSAGE non recapitata a {}: {}", receiverEmail, e.getMessage());
            }
            sendUnreadUpdate(receiverId, receiverEmail);
        }
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload WsMarkReadRequest request, Principal principal) {
        User user = extractUser(principal);
        if (user == null) {
            log.warn("[WS] /chat.read rifiutato: Principal mancante o non valido.");
            return;
        }
        chatAsyncService.markAsReadAsync(request.chatId(), user.getId());
        sendUnreadUpdate(user.getId(), user.getEmail());
    }

    private void sendUnreadUpdate(Long userId, String userEmail) {
        try {
            int count = chatService.getTotalUnreadCount(userId);
            messagingTemplate.convertAndSendToUser(
                    userEmail, "/queue/notifications",
                    new WsUnreadUpdateResponse("UNREAD_UPDATE", userId, count));
        } catch (Exception e) {
            log.warn("[WS] UNREAD_UPDATE non recapitata a {}: {}", userEmail, e.getMessage());
        }
    }

    private User extractUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
