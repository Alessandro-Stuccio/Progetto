package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.facade.impl.AdminFacadeImpl;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.AdminStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFacadeTest {

    @Mock private AdminService adminService;
    @Mock private AdminStatsService adminStatsService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private PlanMapper planMapper;

    @InjectMocks
    private AdminFacadeImpl adminFacade;

    @Test
    @DisplayName("getAllUsers")
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
    @DisplayName("createUser")
    void createUser() {
        UserCreateRequestDTO request = new UserCreateRequestDTO("test@test.com", "Test", "User", "pass", "CLIENT", null, null, null, null);
        User user = new User();
        user.setId(1L);
        UserResponse response = UserResponse.builder().id(1L).build();
        when(adminService.createUser(any())).thenReturn(user);
        when(userMapper.toAdminResponse(user)).thenReturn(response);

        UserResponse result = adminFacade.createUser(request);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deleteUser")
    void deleteUser() {
        adminFacade.deleteUser(1L);
        verify(adminService).deleteUser(1L);
    }

    @Test
    @DisplayName("getAllSubscriptions")
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
    @DisplayName("createPlan")
    void createPlan() {
        PlanCreateRequestDTO request = new PlanCreateRequestDTO("Premium", "MENSILE", 100.0, 100.0, 5, 5);
        Plan plan = new Plan();
        plan.setId(1L);
        PlanResponseDTO response = new PlanResponseDTO(1L, "Premium", "MENSILE", 100.0, 100.0, 5, 5);
        when(adminService.createPlan(any())).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(response);

        PlanResponseDTO result = adminFacade.createPlan(request);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deletePlan")
    void deletePlan() {
        adminFacade.deletePlan(1L);
        verify(adminService).deletePlan(1L);
    }

    @Test
    @DisplayName("getAdminStats")
    void getAdminStats() {
        AdminStatsResponse stats = new AdminStatsResponse(
                Map.of(), 50, List.of(), List.of(), 0L, 0L, null, 0.0, 0.0, 0L, 0L, List.of());
        when(adminStatsService.getAdminStats()).thenReturn(stats);

        assertThat(adminFacade.getAdminStats()).isEqualTo(stats);
    }
}
