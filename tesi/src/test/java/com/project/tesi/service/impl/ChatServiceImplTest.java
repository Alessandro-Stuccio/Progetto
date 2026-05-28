package com.project.tesi.service.impl;

import com.project.tesi.enums.ChatStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import com.project.tesi.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatRepository chatRepository;

    private ChatServiceImpl chatService;

    private User user1, user2, moderator;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(chatRepository);
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

    // ─── closeChat ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("closeChat — chiude la chat e imposta closedBy")
    void closeChat_success() {
        Chat chat = Chat.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now()).build();
        chat.setId(10L);

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(chatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chatService.closeChat(10L, moderator);

        assertThat(chat.getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(chat.getClosedBy()).isEqualTo(moderator);
        verify(chatRepository).save(chat);
    }

}
