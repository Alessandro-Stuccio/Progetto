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
import com.project.tesi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatRepository chatRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private UserService userService;

    private ChatServiceImpl chatService;

    private User user1, user2, moderator;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(chatRepository, messageRepository, userService);
        user1 = User.builder().id(1L).email("u1@test.com").password("password123").firstName("Mario").lastName("Rossi").role(Role.CLIENT).build();
        user2 = User.builder().id(2L).email("u2@test.com").password("password123").firstName("Luca").lastName("Bianchi").role(Role.PERSONAL_TRAINER).build();
        moderator = User.builder().id(3L).email("mod@test.com").password("password123").firstName("Sara").lastName("Verdi").role(Role.MODERATOR).build();
    }

    // ─── getOrCreateChat ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrCreateChat — chat già esistente: restituisce ID senza creare")
    void getOrCreateChat_existingChat() {
        Chat existing = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        existing.setId(10L);
        when(chatRepository.findChatBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        Long chatId = chatService.getOrCreateChat(user1, user2);

        assertThat(chatId).isEqualTo(10L);
        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateChat — nessuna chat: crea e restituisce nuovo ID")
    void getOrCreateChat_newChat() {
        when(chatRepository.findChatBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        Chat saved = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        saved.setId(99L);
        when(chatRepository.save(any())).thenReturn(saved);

        Long chatId = chatService.getOrCreateChat(user1, user2);

        assertThat(chatId).isEqualTo(99L);
        verify(chatRepository).save(any());
    }

    // ─── sendMessage ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendMessage — mittente valido: messaggio salvato")
    void sendMessage_success() {
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);
        SendMessageRequest req = new SendMessageRequest(10L, "Ciao!");

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(userService.getUserById(1L)).thenReturn(user1);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Message result = chatService.sendMessage(req, 1L);
        assertThat(result.getContent()).isEqualTo("Ciao!");
        assertThat(result.isSentByUser1()).isTrue();
    }

    @Test
    @DisplayName("sendMessage — chat CLOSED: lancia ChatNotAllowedException")
    void sendMessage_closedChat() {
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);
        chat.setStatus(ChatStatus.CLOSED);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        assertThatThrownBy(() -> chatService.sendMessage(new SendMessageRequest(10L, "msg"), 1L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("sendMessage — mittente non partecipante: lancia ChatNotAllowedException")
    void sendMessage_notParticipant() {
        User outsider = User.builder().id(99L).email("x@test.com").password("password123").firstName("X").lastName("Y").role(Role.CLIENT).build();
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(userService.getUserById(99L)).thenReturn(outsider);

        assertThatThrownBy(() -> chatService.sendMessage(new SendMessageRequest(10L, "msg"), 99L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    // ─── closeChat ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("closeChat — chiude la chat e imposta closedBy")
    void closeChat_success() {
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);

        when(userService.getUserById(3L)).thenReturn(moderator);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(chatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chatService.closeChat(10L, 3L);

        assertThat(chat.getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(chat.getClosedBy()).isEqualTo(moderator);
        verify(chatRepository).save(chat);
    }

    // ─── getConversation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getConversation — utente non partecipante: lancia ChatNotAllowedException")
    void getConversation_notParticipant() {
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.getConversation(10L, 99L, 0, 20))
                .isInstanceOf(ChatNotAllowedException.class);
    }
}
