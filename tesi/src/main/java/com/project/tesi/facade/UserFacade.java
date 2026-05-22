package com.project.tesi.facade;

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

import java.util.List;

public interface UserFacade {
    ClientDashboardResponse getClientDashboard(Long userId);
    ClientBasicInfoResponse getAdmin();
    ClientBasicInfoResponse getModerator();
    void updateProfile(Long userId, ProfileUpdateRequest request);
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);
    BookingResponse createBooking(BookingRequest request, Long userId);
    void cancelBooking(Long bookingId, Long userId);
    ReviewResponse addReview(ReviewRequest request, Long userId);
    List<ReviewResponse> getReviewsForProfessional(Long professionalId);
    boolean canClientReview(Long clientId, Long professionalId);
    boolean hasClientReviewed(Long clientId, Long professionalId);
    SubscriptionResponse activateSubscription(PlanRequest request, Long userId);
    SubscriptionResponse getSubscriptionStatus(Long userId);
    List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role);
    List<SlotDTO> getAvailableSlots(Long professionalId);
    List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots);
    void deleteSlot(Long slotId, Long requesterId);
    ProfessionalStatsResponse getProfessionalStats(Long professionalId);
}
