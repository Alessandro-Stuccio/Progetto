package com.project.tesi.facade;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;

import java.util.List;

public interface AdminFacade {
    List<UserResponse> getAllUsers();
    UserResponse createUser(UserCreateRequestDTO request);
    void deleteUser(Long id);
    List<SubscriptionResponse> getAllSubscriptions();
    SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri);
    UserResponse updateUser(Long id, ModeratorUserUpdateRequest request);
    PlanResponseDTO createPlan(PlanCreateRequestDTO request);
    PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request);
    void deletePlan(Long id);
    AdminStatsResponse getAdminStats();
}
