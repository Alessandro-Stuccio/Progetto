package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.AdminStatsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminFacadeImpl implements AdminFacade {

    private final AdminService adminService;
    private final AdminStatsService adminStatsService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanMapper planMapper;

    public AdminFacadeImpl(AdminService adminService, AdminStatsService adminStatsService,
                           UserMapper userMapper, SubscriptionMapper subscriptionMapper,
                           PlanMapper planMapper) {
        this.adminService = adminService;
        this.adminStatsService = adminStatsService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.planMapper = planMapper;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers().stream()
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    public UserResponse createUser(UserCreateRequestDTO request) {
        return userMapper.toAdminResponse(adminService.createUser(request));
    }

    @Override
    public void deleteUser(Long id) {
        adminService.deleteUser(id);
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        return adminService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        return subscriptionMapper.toResponse(adminService.updateSubscriptionCredits(id, pt, nutri));
    }

    @Override
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request) {
        return userMapper.toAdminResponse(adminService.updateUser(id, request));
    }

    @Override
    public PlanResponseDTO createPlan(PlanCreateRequestDTO request) {
        return planMapper.toResponse(adminService.createPlan(request));
    }

    @Override
    public PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request) {
        return planMapper.toResponse(adminService.updatePlan(id, request));
    }

    @Override
    public void deletePlan(Long id) {
        adminService.deletePlan(id);
    }

    @Override
    public AdminStatsResponse getAdminStats() {
        return adminStatsService.getAdminStats();
    }
}
