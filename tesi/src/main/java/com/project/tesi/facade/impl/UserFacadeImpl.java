package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.service.BookingService;
import com.project.tesi.service.ProfessionalStatsService;
import com.project.tesi.service.ReviewService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementazione del facade principale per l'area utente.
 */
@Component
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final ProfessionalStatsService professionalStatsService;
    private final SlotService slotService;

    public UserFacadeImpl(UserService userService,
                          BookingService bookingService,
                          ReviewService reviewService,
                          SubscriptionService subscriptionService,
                          ProfessionalStatsService professionalStatsService,
                          SlotService slotService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.professionalStatsService = professionalStatsService;
        this.slotService = slotService;
    }

    @Override
    public ClientDashboardResponse getClientDashboard(Long userId) {
        return userService.getClientDashboard(userId);
    }

    @Override
    public ClientBasicInfoResponse getAdmin() {
        return userService.getAdmin();
    }

    @Override
    public ClientBasicInfoResponse getModerator() {
        return userService.getSupportOperator();
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        userService.updateProfile(userId, request);
    }

    @Override
    public List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId) {
        return userService.getClientsForProfessional(professionalId);
    }

    @Override
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        return bookingService.createBooking(request, userId);
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        bookingService.cancelBooking(bookingId, userId);
    }

    @Override
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        return reviewService.addReview(request, userId);
    }

    @Override
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        return reviewService.getReviewsForProfessional(professionalId);
    }

    @Override
    public boolean canClientReview(Long clientId, Long professionalId) {
        return reviewService.canClientReview(clientId, professionalId);
    }

    @Override
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewService.hasClientReviewed(clientId, professionalId);
    }

    @Override
    public SubscriptionResponse activateSubscription(PlanRequest request, Long userId) {
        return subscriptionService.activateSubscription(request, userId);
    }

    @Override
    public SubscriptionResponse getSubscriptionStatus(Long userId) {
        return subscriptionService.getSubscriptionStatus(userId);
    }

    @Override
    public List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role) {
        return userService.findAvailableProfessionals(role);
    }

    @Override
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        return slotService.getAvailableSlots(professionalId);
    }

    @Override
    public List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots) {
        return slotService.createSlots(professionalId, slots);
    }

    @Override
    public void deleteSlot(Long slotId, Long requesterId) {
        slotService.deleteSlot(slotId, requesterId);
    }

    @Override
    public ProfessionalStatsResponse getProfessionalStats(Long professionalId) {
        return professionalStatsService.getProfessionalStats(professionalId);
    }

}
