package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.Role;

import java.util.List;

public interface UserFacade {
    UserResponse registerUser(RegisterRequest request);
    ClientDashboardResponse getClientDashboard(Long userId);
    ClientBasicInfoResponse getAdmin();
    ClientBasicInfoResponse getModerator();
    void updateProfile(Long userId, ProfileUpdateRequest request);
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);
    SubscriptionResponse activateSubscription(PlanRequest request, Long userId);
    SubscriptionResponse getSubscriptionStatus(Long userId);
    List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role);
    ProfessionalStatsResponse getProfessionalStats(Long professionalId);
}
