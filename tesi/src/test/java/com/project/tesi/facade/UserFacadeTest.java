package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.UserFacadeImpl;
import com.project.tesi.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock private UserService userService;
    @Mock private BookingService bookingService;
    @Mock private ReviewService reviewService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private ActivityFeedService activityFeedService; // kept for injection compatibility
    @Mock private ProfessionalStatsService professionalStatsService;
    @Mock private SlotService slotService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    @DisplayName("getClientDashboard — delega al UserService")
    void getClientDashboard() {
        ClientDashboardResponse resp = ClientDashboardResponse.builder().build();
        when(userService.getClientDashboard(1L)).thenReturn(resp);

        assertThat(userFacade.getClientDashboard(1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("updateProfile — delega al UserService")
    void updateProfile() {
        ProfileUpdateRequest req = new ProfileUpdateRequest(null, null, null, null);
        userFacade.updateProfile(1L, req);
        verify(userService).updateProfile(1L, req);
    }

    @Test
    @DisplayName("getClientsForProfessional — delega al UserService")
    void getClientsForProfessional() {
        List<ClientBasicInfoResponse> clients = List.of(
                ClientBasicInfoResponse.builder().id(1L).build());
        when(userService.getClientsForProfessional(2L)).thenReturn(clients);

        assertThat(userFacade.getClientsForProfessional(2L)).isEqualTo(clients);
    }

    @Test
    @DisplayName("createBooking — delega al BookingService")
    void createBooking() {
        BookingRequest req = new BookingRequest(10L);
        BookingResponse resp = BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build();
        when(bookingService.createBooking(req, 1L)).thenReturn(resp);

        assertThat(userFacade.createBooking(req, 1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("addReview — delega al ReviewService")
    void addReview() {
        ReviewRequest req = new ReviewRequest(2L, 5, "test");
        ReviewResponse resp = ReviewResponse.builder().rating(5).build();
        when(reviewService.addReview(req, 1L)).thenReturn(resp);

        assertThat(userFacade.addReview(req, 1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("getReviewsForProfessional — delega al ReviewService")
    void getReviewsForProfessional() {
        when(reviewService.getReviewsForProfessional(2L)).thenReturn(List.of());
        assertThat(userFacade.getReviewsForProfessional(2L)).isEmpty();
    }

    @Test
    @DisplayName("canClientReview — delega al ReviewService")
    void canClientReview() {
        when(reviewService.canClientReview(1L, 2L)).thenReturn(true);
        assertThat(userFacade.canClientReview(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("hasClientReviewed — delega al ReviewService")
    void hasClientReviewed() {
        when(reviewService.hasClientReviewed(1L, 2L)).thenReturn(false);
        assertThat(userFacade.hasClientReviewed(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("activateSubscription — delega al SubscriptionService")
    void activateSubscription() {
        PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        SubscriptionResponse resp = SubscriptionResponse.builder().id(1L).build();
        when(subscriptionService.activateSubscription(req, 1L)).thenReturn(resp);

        assertThat(userFacade.activateSubscription(req, 1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("getSubscriptionStatus — delega al SubscriptionService")
    void getSubscriptionStatus() {
        SubscriptionResponse resp = SubscriptionResponse.builder().active(true).build();
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(resp);

        assertThat(userFacade.getSubscriptionStatus(1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("findAvailableProfessionals — delega al UserService")
    void findAvailableProfessionals() {
        when(userService.findAvailableProfessionals(Role.PERSONAL_TRAINER)).thenReturn(List.of());
        assertThat(userFacade.findAvailableProfessionals(Role.PERSONAL_TRAINER)).isEmpty();
    }

    @Test
    @DisplayName("getAvailableSlots — delega al SlotService")
    void getAvailableSlots() {
        when(slotService.getAvailableSlots(2L)).thenReturn(List.of());
        assertThat(userFacade.getAvailableSlots(2L)).isEmpty();
    }

    @Test
    @DisplayName("createSlots — delega al SlotService")
    void createSlots() {
        List<SlotDTO> slots = List.of();
        when(slotService.createSlots(2L, slots)).thenReturn(List.of());
        assertThat(userFacade.createSlots(2L, slots)).isEmpty();
    }

    @Test
    @DisplayName("deleteSlot — delega al SlotService con requesterId")
    void deleteSlot() {
        userFacade.deleteSlot(10L, 2L);
        verify(slotService).deleteSlot(10L, 2L);
    }

    @Test
    @DisplayName("getProfessionalStats — delega al ProfessionalStatsService")
    void getProfessionalStats() {
        ProfessionalStatsResponse stats = new ProfessionalStatsResponse(List.of(), 0, List.of(), 0, 0, 5);
        when(professionalStatsService.getProfessionalStats(2L)).thenReturn(stats);

        assertThat(userFacade.getProfessionalStats(2L)).isEqualTo(stats);
    }

}

