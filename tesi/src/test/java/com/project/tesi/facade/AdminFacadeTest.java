package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.user.AdminSelfDeletionNotAllowedException;
import com.project.tesi.facade.impl.AdminFacadeImpl;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFacadeTest {

    @Mock private AdminService adminService;
    @Mock private AdminStatsService adminStatsService;
    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private PlanService planService;
    @Mock private SlotService slotService;
    @Mock private ChatService chatService;
    @Mock private ReviewService reviewService;
    @Mock private DocumentService documentService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private PlanMapper planMapper;

    @InjectMocks
    private AdminFacadeImpl adminFacade;

    @Test
    @DisplayName("getAllUsers — mappa tutti gli utenti a UserResponse")
    void getAllUsers() {
        User user = new User();
        user.setId(1L);
        UserResponse response = UserResponse.builder().id(1L).build();
        when(adminService.getAllUsers()).thenReturn(List.of(user));
        when(userMapper.toAdminResponse(user)).thenReturn(response);

        List<UserResponse> result = adminFacade.getAllUsers();
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createUser — crea MODERATOR senza subscription")
    void createUser_moderator() {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                "mod@test.com", "Test", "Mod", "password123", "MODERATOR", null, null, null, null);
        User saved = User.builder().id(1L).email("mod@test.com").password("encodedpass").role(Role.MODERATOR)
                .firstName("Test").lastName("Mod").build();
        UserResponse response = UserResponse.builder().id(1L).build();

        when(adminService.existsUserByEmail("mod@test.com")).thenReturn(false);
        when(userService.encodePassword("password123")).thenReturn("encodedpass");
        when(adminService.saveUser(any())).thenReturn(saved);
        when(userMapper.toAdminResponse(saved)).thenReturn(response);

        UserResponse result = adminFacade.createUser(request);
        assertThat(result.getId()).isEqualTo(1L);
        verify(subscriptionService, never()).activateSubscription(any(), any());
    }

    @Test
    @DisplayName("createUser — CLIENT con planId attiva subscription")
    void createUser_clientWithPlan() {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                "client@test.com", "New", "Client", "password123", "CLIENT", null, null, 1L, "UNICA_SOLUZIONE");
        User saved = User.builder().id(2L).email("client@test.com").password("encodedpass").role(Role.CLIENT)
                .firstName("New").lastName("Client").build();
        UserResponse response = UserResponse.builder().id(2L).build();
        Subscription sub = new Subscription();

        when(adminService.existsUserByEmail("client@test.com")).thenReturn(false);
        when(userService.encodePassword("password123")).thenReturn("encodedpass");
        when(adminService.saveUser(any())).thenReturn(saved);
        when(subscriptionService.activateSubscription(any(), eq(2L))).thenReturn(sub);
        when(userMapper.toAdminResponse(saved)).thenReturn(response);

        UserResponse result = adminFacade.createUser(request);
        assertThat(result.getId()).isEqualTo(2L);
        verify(subscriptionService).activateSubscription(any(), eq(2L));
    }

    @Test
    @DisplayName("createUser — ruolo ADMIN lancia UnauthorizedAccessException")
    void createUser_adminRoleThrows() {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                "a@test.com", "X", "Y", "password123", "ADMIN", null, null, null, null);
        assertThatThrownBy(() -> adminFacade.createUser(request))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(adminService, never()).saveUser(any());
    }

    @Test
    @DisplayName("createUser — campi obbligatori mancanti lancia IllegalArgumentException")
    void createUser_missingFields_throws() {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                null, "X", "Y", "password123", "MODERATOR", null, null, null, null);
        assertThatThrownBy(() -> adminFacade.createUser(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(adminService, never()).saveUser(any());
    }

    @Test
    @DisplayName("createUser — email duplicata lancia ResourceAlreadyExistsException")
    void createUser_emailDuplicate_throws() {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                "dup@test.com", "X", "Y", "password123", "MODERATOR", null, null, null, null);
        when(adminService.existsUserByEmail("dup@test.com")).thenReturn(true);
        assertThatThrownBy(() -> adminFacade.createUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(adminService, never()).saveUser(any());
    }

    @Test
    @DisplayName("updateUser — ADMIN target lancia UnauthorizedAccessException")
    void updateUser_adminTargetThrows() {
        User admin = User.builder().id(1L).email("admin@test.com").password("password123")
                .role(Role.ADMIN).firstName("Admin").lastName("User").build();
        when(userService.getUserById(1L)).thenReturn(admin);
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(null, null, null, null, null);

        assertThatThrownBy(() -> adminFacade.updateUser(1L, request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("updateUser — CLIENT target lancia UnauthorizedAccessException (fuori da ADMIN_MANAGEABLE_ROLES)")
    void updateUser_clientTargetThrows() {
        User client = User.builder().id(2L).email("client@test.com").password("password123")
                .role(Role.CLIENT).firstName("Cl").lastName("Ient").build();
        when(userService.getUserById(2L)).thenReturn(client);
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(null, null, null, null, null);

        assertThatThrownBy(() -> adminFacade.updateUser(2L, request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("updateUser — MODERATOR target aggiornato con successo")
    void updateUser_moderatorSuccess() {
        User moderator = User.builder().id(3L).email("mod@test.com").password("password123")
                .role(Role.MODERATOR).firstName("Mo").lastName("Der").build();
        User updated = User.builder().id(3L).email("new@test.com").password("password123")
                .role(Role.MODERATOR).firstName("Mo").lastName("Der").build();
        UserResponse response = UserResponse.builder().id(3L).build();
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest("new@test.com", null, null, null, null);

        when(userService.getUserById(3L)).thenReturn(moderator);
        when(adminService.existsUserByEmailExcluding("new@test.com", 3L)).thenReturn(false);
        when(adminService.saveUser(any())).thenReturn(updated);
        when(userMapper.toAdminResponse(updated)).thenReturn(response);

        assertThat(adminFacade.updateUser(3L, request).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("deleteUser — target non-ADMIN viene eliminato")
    void deleteUser_success() {
        User moderator = User.builder().id(5L).email("mod@test.com").password("password123")
                .role(Role.MODERATOR).firstName("Mo").lastName("Der").build();
        when(userService.getUserById(5L)).thenReturn(moderator);

        adminFacade.deleteUser(5L);
        verify(adminService).deleteUser(5L);
    }

    @Test
    @DisplayName("deleteUser — self-deletion di ADMIN lancia AdminSelfDeletionNotAllowedException")
    void deleteUser_selfDeletion() {
        User admin = User.builder().id(1L).email("admin@test.com").password("password123")
                .role(Role.ADMIN).firstName("Admin").lastName("User").build();
        when(userService.getUserById(1L)).thenReturn(admin);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of()));

        try {
            assertThatThrownBy(() -> adminFacade.deleteUser(1L))
                    .isInstanceOf(AdminSelfDeletionNotAllowedException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("deleteUser — eliminazione di altro ADMIN lancia UnauthorizedAccessException")
    void deleteUser_otherAdmin() {
        User admin = User.builder().id(2L).email("other@test.com").password("password123")
                .role(Role.ADMIN).firstName("Other").lastName("Admin").build();
        when(userService.getUserById(2L)).thenReturn(admin);

        assertThatThrownBy(() -> adminFacade.deleteUser(2L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("getAllSubscriptions — mappa abbonamenti a DTO")
    void getAllSubscriptions() {
        Subscription sub = new Subscription();
        sub.setId(1L);
        SubscriptionResponse response = SubscriptionResponse.builder().id(1L).build();
        when(adminService.getAllSubscriptions()).thenReturn(List.of(sub));
        when(subscriptionMapper.toResponse(sub)).thenReturn(response);

        List<SubscriptionResponse> result = adminFacade.getAllSubscriptions();
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateSubscriptionCredits — crediti negativi lancia IllegalArgumentException")
    void updateSubscriptionCredits_negativePT_throws() {
        assertThatThrownBy(() -> adminFacade.updateSubscriptionCredits(1L, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        verify(adminService, never()).updateSubscriptionCredits(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("createPlan — delega al service e mappa")
    void createPlan() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO("Premium", "ANNUALE", 100.0, 100.0, 5, 5);
        Plan plan = Plan.builder().id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .fullPrice(100.0).monthlyInstallmentPrice(100.0).build();
        PlanResponseDTO response = new PlanResponseDTO(1L, "Premium", "ANNUALE", 100.0, 100.0, 5, 5);

        when(planService.existsByName("Premium")).thenReturn(false);
        when(planMapper.toPlan(request)).thenReturn(plan);
        when(planService.createPlan(any())).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(response);

        PlanResponseDTO result = adminFacade.createPlan(request);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createPlan — campi mancanti lancia IllegalArgumentException")
    void createPlan_missingFields_throws() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO(null, "ANNUALE", 100.0, 100.0, 5, 5);
        assertThatThrownBy(() -> adminFacade.createPlan(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("deletePlan — piano con sottoscrittori attivi lancia IllegalStateException")
    void deletePlan_hasSubscribers_throws() {
        when(planService.hasSubscribers(1L)).thenReturn(true);
        assertThatThrownBy(() -> adminFacade.deletePlan(1L))
                .isInstanceOf(IllegalStateException.class);
        verify(planService, never()).deletePlan(any());
    }

    @Test
    @DisplayName("deletePlan — delega al service")
    void deletePlan() {
        when(planService.hasSubscribers(1L)).thenReturn(false);
        adminFacade.deletePlan(1L);
        verify(planService).deletePlan(1L);
    }

    @Test
    @DisplayName("getAdminStats — aggrega dati dai 4 metodi raw di AdminStatsService")
    void getAdminStats() {
        when(adminService.getAllUsers()).thenReturn(List.of());
        when(adminService.getAllSubscriptions()).thenReturn(List.of());
        when(adminStatsService.getAllPlans()).thenReturn(List.of());
        when(adminStatsService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = adminFacade.getAdminStats();
        assertThat(result).isNotNull();
        assertThat(result.totalUsers()).isEqualTo(0);
        assertThat(result.totalActiveSubscriptions()).isEqualTo(0L);
    }
}
