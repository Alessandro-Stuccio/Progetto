package com.project.tesi.controller;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.UpdateNotesRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.facade.BookingFacade;
import com.project.tesi.facade.ChatFacade;
import com.project.tesi.facade.DocumentFacade;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.facade.ActivityFeedFacade;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.EmailService;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.dto.request.JobApplicationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllControllersTest {

    @Nested
    class AdminControllerTests {
        @Mock private AdminFacade adminFacade;
        @InjectMocks private AdminController adminController;

        @Test @DisplayName("getAllUsers")
        void getAllUsers() {
            when(adminFacade.getAllUsers()).thenReturn(List.of(UserResponse.builder().id(1L).build()));
            ResponseEntity<List<UserResponse>> resp = adminController.getAllUsers();
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).hasSize(1);
        }

        @Test @DisplayName("createUser")
        void createUser() {
            UserCreateRequestDTO body = new UserCreateRequestDTO("test@test.com", "test", "test", "test", "CLIENT", null, null, null, null);
            when(adminFacade.createUser(body)).thenReturn(UserResponse.builder().id(1L).build());
            ResponseEntity<UserResponse> resp = adminController.createUser(body);
            assertThat(resp.getBody().getId()).isEqualTo(1L);
        }

        @Test @DisplayName("deleteUser")
        void deleteUser() {
            ResponseEntity<Map<String, String>> resp = adminController.deleteUser(1L);
            verify(adminFacade).deleteUser(1L);
            assertThat(resp.getBody().get("message")).contains("deleted");
        }

        @Test @DisplayName("getAllSubscriptions")
        void getAllSubscriptions() {
            when(adminFacade.getAllSubscriptions()).thenReturn(List.of());
            assertThat(adminController.getAllSubscriptions().getBody()).isEmpty();
        }

        @Test @DisplayName("createPlan")
        void createPlan() {
            PlanCreateRequestDTO body = new PlanCreateRequestDTO("Premium", "MENSILE", 100.0, 100.0, 5, 5);
            when(adminFacade.createPlan(body)).thenReturn(new PlanResponseDTO(1L, null, null, null, null, null, null));
            assertThat(adminController.createPlan(body).getBody().id()).isEqualTo(1L);
        }

        @Test @DisplayName("deletePlan")
        void deletePlan() {
            ResponseEntity<Map<String, String>> resp = adminController.deletePlan(1L);
            verify(adminFacade).deletePlan(1L);
            assertThat(resp.getBody().get("message")).contains("deleted");
        }
    }

    @Nested
    class AdminStatsControllerTests {
        @Mock private AdminFacade adminFacade;
        @InjectMocks private AdminStatsController adminStatsController;

        @Test @DisplayName("getStats")
        void getStats() {
            AdminStatsResponse stats = new AdminStatsResponse(
                    Map.of(), 50, List.of(), List.of(), 0L, 0L, null, 0.0, 0.0, 0L, 0L, List.of());
            when(adminFacade.getAdminStats()).thenReturn(stats);
            ResponseEntity<AdminStatsResponse> resp = adminStatsController.getStats();
            assertThat(resp.getBody().totalUsers()).isEqualTo(50);
        }
    }

    @Nested
    class UserControllerTests {
        @Mock private UserFacade userFacade;
        @InjectMocks private UserController userController;

        @Test @DisplayName("getDashboard")
        void getDashboard() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            ClientDashboardResponse dash = ClientDashboardResponse.builder().build();
            when(userFacade.getClientDashboard(1L)).thenReturn(dash);
            assertThat(userController.getDashboard(mockUser).getBody()).isEqualTo(dash);
        }

        @Test @DisplayName("getClientsForProfessional")
        void getClientsForProfessional() {
            User mockUser = User.builder().id(2L).email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
            when(userFacade.getClientsForProfessional(2L)).thenReturn(List.of());
            assertThat(userController.getClientsForProfessional(mockUser).getBody()).isEmpty();
        }

        @Test @DisplayName("updateProfile")
        void updateProfile() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            ProfileUpdateRequest req = new ProfileUpdateRequest(null, null, null, null);
            ResponseEntity<Void> resp = userController.updateProfile(mockUser, req);
            verify(userFacade).updateProfile(1L, req);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }
    }

    @Nested
    class SubscriptionControllerTests {
        @Mock private UserFacade userFacade;
        @InjectMocks private SubscriptionController subscriptionController;

        @Test @DisplayName("activateSubscription")
        void activateSubscription() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
            SubscriptionResponse resp = SubscriptionResponse.builder().id(1L).active(true).build();
            when(userFacade.activateSubscription(req, 1L)).thenReturn(resp);
            assertThat(subscriptionController.activateSubscription(req, mockUser).getBody().isActive()).isTrue();
        }

        @Test @DisplayName("getSubscriptionStatus")
        void getSubscriptionStatus() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            SubscriptionResponse resp = SubscriptionResponse.builder().id(1L).build();
            when(userFacade.getSubscriptionStatus(1L)).thenReturn(resp);
            assertThat(subscriptionController.getSubscriptionStatus(mockUser).getBody().getId()).isEqualTo(1L);
        }
    }

    @Nested
    class ProfessionalControllerTests {
        @Mock private UserFacade userFacade;
        @Mock private BookingFacade bookingFacade;
        @InjectMocks private ProfessionalController professionalController;

        @Test @DisplayName("getProfessionals")
        void getProfessionals() {
            when(userFacade.findAvailableProfessionals(Role.PERSONAL_TRAINER)).thenReturn(List.of());
            assertThat(professionalController.getProfessionals(Role.PERSONAL_TRAINER).getBody()).isEmpty();
        }

        @Test @DisplayName("getProfessionalSlots")
        void getProfessionalSlots() {
            when(bookingFacade.getAvailableSlots(2L)).thenReturn(List.of());
            assertThat(professionalController.getProfessionalSlots(2L).getBody()).isEmpty();
        }

        @Test @DisplayName("createSlots")
        void createSlots() {
            User mockUser = User.builder().id(2L).email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
            List<SlotDTO> slots = List.of();
            when(bookingFacade.createSlots(2L, slots)).thenReturn(List.of());
            assertThat(professionalController.createSlots(mockUser, slots).getBody()).isEmpty();
        }

        @Test @DisplayName("deleteSlot")
        void deleteSlot() {
            User mockUser = User.builder().id(2L).email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
            ResponseEntity<Void> resp = professionalController.deleteSlot(10L, mockUser);
            verify(bookingFacade).deleteSlot(10L, 2L);
            assertThat(resp.getStatusCode().value()).isEqualTo(204);
        }
    }

    @Nested
    class ProfessionalStatsControllerTests {
        @Mock private UserFacade userFacade;
        @InjectMocks private ProfessionalStatsController professionalStatsController;

        @Test @DisplayName("getStats")
        void getStats() {
            User mockUser = User.builder().id(2L).email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
            ProfessionalStatsResponse stats = new ProfessionalStatsResponse(List.of(), 0, List.of(), 0, 0, 5);
            when(userFacade.getProfessionalStats(2L)).thenReturn(stats);
            assertThat(professionalStatsController.getStats(mockUser).getBody().totalClients()).isEqualTo(5);
        }
    }

    @Nested
    class ActivityFeedControllerTests {
        @Mock private ActivityFeedFacade activityFeedFacade;
        @InjectMocks private ActivityFeedController activityFeedController;

        @Test @DisplayName("getActivityFeed")
        void getActivityFeed() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            ActivityFeedItemResponse item = new ActivityFeedItemResponse("booking", "text", java.time.LocalDateTime.now());
            when(activityFeedFacade.getActivityFeed(1L, 14, 15)).thenReturn(List.of(item));
            ResponseEntity<List<ActivityFeedItemResponse>> resp = activityFeedController.getActivityFeed(mockUser, 14, 15);
            assertThat(resp.getBody()).hasSize(1);
        }
    }

    @Nested
    class ChatControllerTest {
        @Mock private ChatFacade chatFacade;
        private com.project.tesi.controller.ChatController controller;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new com.project.tesi.controller.ChatController(chatFacade);
        }

        @Test @DisplayName("sendMessage")
        void sendMessage() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            SendMessageRequest req = new SendMessageRequest(1L, "test");
            ChatMessageResponse resp = ChatMessageResponse.builder().id(1L).build();
            when(chatFacade.sendMessage(req, 1L)).thenReturn(resp);
            assertThat(controller.sendMessage(mockUser, req).getBody().getId()).isEqualTo(1L);
        }

        @Test @DisplayName("getConversation")
        void getConversation() {
            User mockUser = User.builder().id(2L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            when(chatFacade.getConversation(1L, 2L, 0, 50)).thenReturn(List.of());
            assertThat(controller.getConversation(mockUser, 1L, 0, 50).getBody()).isEmpty();
        }

        @Test @DisplayName("getUserConversations")
        void getUserConversations() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            when(chatFacade.getUserConversations(1L)).thenReturn(List.of());
            assertThat(controller.getUserConversations(mockUser).getBody()).isEmpty();
        }

        @Test @DisplayName("markAsRead")
        void markAsRead() {
            User mockUser = User.builder().id(2L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            ResponseEntity<Void> resp = controller.markAsRead(mockUser, 1L);
            verify(chatFacade).markAsRead(1L, 2L);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
        }

        @Test @DisplayName("getTotalUnreadCount")
        void getTotalUnreadCount() {
            User mockUser = User.builder().id(1L).email("test@test.com").password("testpass").role(Role.CLIENT).build();
            when(chatFacade.getTotalUnreadCount(1L)).thenReturn(5);
            assertThat(controller.getTotalUnreadCount(mockUser).getBody()).isEqualTo(5);
        }
    }

    @Nested
    class DocumentControllerTest {
        @Mock private DocumentFacade documentFacade;
        private com.project.tesi.controller.DocumentController controller;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            controller = new com.project.tesi.controller.DocumentController(documentFacade);
        }

        @Test @DisplayName("downloadFile")
        void downloadFile() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            Document doc = Document.builder().id(1L).fileName("test.pdf").contentType("application/pdf").build();
            when(documentFacade.getDocumentById(1L)).thenReturn(doc);
            when(documentFacade.downloadDocumentSecure(1L, 1L)).thenReturn(new byte[]{1, 2, 3});

            ResponseEntity<byte[]> resp = controller.downloadFile(1L, mockUser);
            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).hasSize(3);
        }

        @Test @DisplayName("downloadFile — contentType null usa application/octet-stream")
        void downloadFile_nullContentType() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            Document doc = Document.builder().id(1L).fileName("file.bin").contentType(null).build();
            when(documentFacade.getDocumentById(1L)).thenReturn(doc);
            when(documentFacade.downloadDocumentSecure(1L, 1L)).thenReturn(new byte[]{});

            ResponseEntity<byte[]> resp = controller.downloadFile(1L, mockUser);
            assertThat(resp.getHeaders().getContentType().toString()).isEqualTo("application/octet-stream");
        }

        @Test @DisplayName("getUserDocuments")
        void getUserDocuments() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            when(documentFacade.getUserDocumentsDtoSecure(1L, 1L)).thenReturn(List.of());
            assertThat(controller.getUserDocuments(1L, mockUser).getBody()).isEmpty();
        }

        @Test @DisplayName("getUserDocumentsByType")
        void getUserDocumentsByType() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            when(documentFacade.getUserDocumentsByTypeDtoSecure(1L, "WORKOUT_PLAN", 1L)).thenReturn(List.of());
            assertThat(controller.getUserDocumentsByType(1L, "WORKOUT_PLAN", mockUser).getBody()).isEmpty();
        }

        @Test @DisplayName("deleteDocument")
        void deleteDocument() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            ResponseEntity<Void> resp = controller.deleteDocument(1L, mockUser);
            verify(documentFacade).deleteDocument(1L, 1L);
            assertThat(resp.getStatusCode().value()).isEqualTo(204);
        }

        @Test @DisplayName("updateNotes")
        void updateNotes() {
            User mockUser = User.builder().id(1L).email("client@test.com").password("testpass").role(Role.CLIENT).build();
            UpdatedNotesResponse updated = new UpdatedNotesResponse(1L, "nuove note");
            when(documentFacade.updateNotes(1L, "nuove note", 1L)).thenReturn(updated);
            ResponseEntity<UpdatedNotesResponse> resp = controller.updateNotes(1L, new UpdateNotesRequest("nuove note"), mockUser);
            assertThat(resp.getBody().notes()).isEqualTo("nuove note");
        }
    }

    @Nested
    class JobApplicationControllerTests {
        @Mock private EmailService emailService;
        @InjectMocks private JobApplicationController jobApplicationController;

        @Test @DisplayName("submitApplication")
        void submitApplication() {
            JobApplicationRequest req = new JobApplicationRequest("Mario", "Rossi", "mario@test.com", "Dev", "Motivazione");
            ResponseEntity<Map<String, String>> resp = jobApplicationController.submitApplication(req, null);
            verify(emailService).sendJobApplication(req, null);
            assertThat(resp.getBody().get("message")).contains("Candidatura");
        }
    }
}
