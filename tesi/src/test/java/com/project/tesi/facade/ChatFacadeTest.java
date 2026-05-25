package com.project.tesi.facade;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.chat.ChatNotAllowedException;
import com.project.tesi.facade.impl.ChatFacadeImpl;
import com.project.tesi.mapper.ChatMapper;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatFacadeTest {

    @Mock private ChatService chatService;
    @Mock private ChatMapper chatMapper;
    @Mock private UserService userService;

    @InjectMocks private ChatFacadeImpl chatFacade;

    private User buildUser(Long id, Role role) {
        return User.builder()
                .id(id)
                .email(role.name().toLowerCase() + id + "@test.com")
                .password("password123")
                .firstName("Test").lastName("User")
                .role(role)
                .build();
    }

    // ─── createChat: self-chat ────────────────────────────────────────────────

    @Test
    @DisplayName("createChat — stessa persona (senderId == receiverId): lancia IllegalArgumentException")
    void createChat_selfChat() {
        assertThatThrownBy(() -> chatFacade.createChat(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userService, never()).getUserById(any());
    }

    // ─── createChat: ADMIN ────────────────────────────────────────────────────

    @Test
    @DisplayName("createChat — destinatario ADMIN: sempre consentito")
    void createChat_adminReceiverAllowed() {
        User client = buildUser(1L, Role.CLIENT);
        User admin = buildUser(2L, Role.ADMIN);

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(admin);
        when(chatService.getOrCreateChat(client, admin)).thenReturn(42L);

        Long chatId = chatFacade.createChat(1L, 2L);
        assertThat(chatId).isEqualTo(42L);
    }

    // ─── createChat: INSURANCE_MANAGER ────────────────────────────────────────

    @Test
    @DisplayName("createChat — INSURANCE_MANAGER contatta non-ADMIN: lancia ChatNotAllowedException")
    void createChat_insuranceManagerBlockedWithNonAdmin() {
        User insurance = buildUser(1L, Role.INSURANCE_MANAGER);
        User moderator = buildUser(2L, Role.MODERATOR);

        when(userService.getUserById(1L)).thenReturn(insurance);
        when(userService.getUserById(2L)).thenReturn(moderator);

        assertThatThrownBy(() -> chatFacade.createChat(1L, 2L))
                .isInstanceOf(ChatNotAllowedException.class);
        verify(chatService, never()).getOrCreateChat(any(), any());
    }

    // ─── createChat: MODERATOR ────────────────────────────────────────────────

    @Test
    @DisplayName("createChat — MODERATOR con qualsiasi utente: sempre consentito")
    void createChat_moderatorAllowed() {
        User moderator = buildUser(1L, Role.MODERATOR);
        User client = buildUser(2L, Role.CLIENT);

        when(userService.getUserById(1L)).thenReturn(moderator);
        when(userService.getUserById(2L)).thenReturn(client);
        when(chatService.getOrCreateChat(moderator, client)).thenReturn(10L);

        assertThat(chatFacade.createChat(1L, 2L)).isEqualTo(10L);
    }

    // ─── createChat: CLIENT ↔ PERSONAL_TRAINER ────────────────────────────────

    @Test
    @DisplayName("createChat — CLIENT con PT assegnato: consentito")
    void createChat_assignedPtAllowed() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        User client = User.builder()
                .id(1L).email("client@test.com").password("password123")
                .firstName("C").lastName("L").role(Role.CLIENT)
                .assignedPT(pt)
                .build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(pt);
        when(chatService.getOrCreateChat(client, pt)).thenReturn(5L);

        assertThat(chatFacade.createChat(1L, 2L)).isEqualTo(5L);
    }

    @Test
    @DisplayName("createChat — CLIENT senza PT assegnato: lancia ChatNotAllowedException")
    void createChat_unassignedPtBlocked() {
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        User client = buildUser(1L, Role.CLIENT); // no assignedPT

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(pt);

        assertThatThrownBy(() -> chatFacade.createChat(1L, 2L))
                .isInstanceOf(ChatNotAllowedException.class);
        verify(chatService, never()).getOrCreateChat(any(), any());
    }

    @Test
    @DisplayName("createChat — CLIENT con PT diverso da quello assegnato: lancia ChatNotAllowedException")
    void createChat_differentPtBlocked() {
        User otherPt = buildUser(99L, Role.PERSONAL_TRAINER);
        User assignedPt = buildUser(2L, Role.PERSONAL_TRAINER);
        User client = User.builder()
                .id(1L).email("client@test.com").password("password123")
                .firstName("C").lastName("L").role(Role.CLIENT)
                .assignedPT(otherPt) // assigned to a different PT
                .build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(assignedPt);

        assertThatThrownBy(() -> chatFacade.createChat(1L, 2L))
                .isInstanceOf(ChatNotAllowedException.class);
    }
}
