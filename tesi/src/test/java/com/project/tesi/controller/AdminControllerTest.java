package com.project.tesi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.SubscriptionCreditsUpdateDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.GlobalExceptionHandler;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    AdminFacade adminFacade;

    @InjectMocks
    AdminController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withAdminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);
        admin.setFirstName("Admin");
        admin.setLastName("User");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        withAdminUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/admin/users

    @Test
    @DisplayName("GET /api/admin/users — 200 con lista utenti")
    void getManageableUsers_returns200() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(2L).firstName("Luca").lastName("Rossi").email("luca@test.com").role(Role.CLIENT)
                .build();
        when(adminFacade.getManageableUsers(any(User.class))).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users").with(withAdminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].email").value("luca@test.com"));
    }

    // ------------------------------------------------------------------ POST /api/admin/users

    @Test
    @DisplayName("POST /api/admin/users — 200 quando utente creato con successo")
    void createUser_returns200() throws Exception {
        UserResponse created = UserResponse.builder()
                .id(10L).firstName("Mario").lastName("Bianchi").email("mario@test.com").role(Role.CLIENT)
                .build();
        when(adminFacade.createUser(any(UserCreateRequestDTO.class), any(User.class))).thenReturn(created);

        UserCreateRequestDTO req = new UserCreateRequestDTO(
                "mario@test.com", "Mario", "Bianchi", "password123", "CLIENT",
                null, null, null, null);

        mockMvc.perform(post("/api/admin/users")
                        .with(withAdminUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("mario@test.com"));
    }

    // ------------------------------------------------------------------ PUT /api/admin/users/{id}

    @Test
    @DisplayName("PUT /api/admin/users/{id} — 200 quando aggiornamento ha successo")
    void updateUser_returns200() throws Exception {
        UserResponse updated = UserResponse.builder()
                .id(2L).firstName("Luca").lastName("Verdi").email("luca@test.com").role(Role.CLIENT)
                .build();
        when(adminFacade.updateUser(anyLong(), any(ModeratorUserUpdateRequest.class), any(User.class)))
                .thenReturn(updated);

        ModeratorUserUpdateRequest req = new ModeratorUserUpdateRequest(
                "luca@test.com", "Luca", "Verdi", null);

        mockMvc.perform(put("/api/admin/users/2")
                        .with(withAdminUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Verdi"));
    }

    // ------------------------------------------------------------------ DELETE /api/admin/users/{id}

    @Test
    @DisplayName("DELETE /api/admin/users/{id} — 200 con messaggio di conferma")
    void deleteUser_returns200() throws Exception {
        doNothing().when(adminFacade).deleteUser(anyLong(), any(User.class));

        mockMvc.perform(delete("/api/admin/users/2").with(withAdminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utente disabilitato"));
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} — 404 quando utente non trovato")
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Utente", 99L))
                .when(adminFacade).deleteUser(anyLong(), any(User.class));

        mockMvc.perform(delete("/api/admin/users/99").with(withAdminUser))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/admin/subscriptions

    @Test
    @DisplayName("GET /api/admin/subscriptions — 200 con lista abbonamenti")
    void getAllSubscriptions_returns200() throws Exception {
        SubscriptionResponse sub = SubscriptionResponse.builder()
                .id(1L).userId(2L).userName("Luca Rossi").planName("Premium").active(true)
                .build();
        when(adminFacade.getAllSubscriptions()).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/admin/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].planName").value("Premium"));
    }

    // ------------------------------------------------------------------ PUT /api/admin/subscriptions/{id}/credits

    @Test
    @DisplayName("PUT /api/admin/subscriptions/{id}/credits — 200 con crediti aggiornati")
    void updateSubscriptionCredits_returns200() throws Exception {
        SubscriptionResponse updated = SubscriptionResponse.builder()
                .id(1L).currentCreditsPT(3).currentCreditsNutri(2).active(true)
                .build();
        when(adminFacade.updateSubscriptionCredits(anyLong(), any(int.class), any(int.class)))
                .thenReturn(updated);

        SubscriptionCreditsUpdateDTO req = new SubscriptionCreditsUpdateDTO(3, 2);

        mockMvc.perform(put("/api/admin/subscriptions/1/credits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCreditsPT").value(3));
    }

    // ------------------------------------------------------------------ POST /api/admin/plans

    @Test
    @DisplayName("POST /api/admin/plans — 200 con piano creato")
    void createPlan_returns200() throws Exception {
        PlanResponseDTO plan = PlanResponseDTO.builder()
                .id(5L).name("Gold").duration("ANNUAL").fullPrice(299.0).monthlyInstallmentPrice(29.9)
                .monthlyCreditsPT(2).monthlyCreditsNutri(2)
                .build();
        when(adminFacade.createPlan(any(PlanCreateRequestDTO.class))).thenReturn(plan);

        PlanCreateRequestDTO req = new PlanCreateRequestDTO("Gold", "ANNUAL", 299.0, 29.9, 2, 2);

        mockMvc.perform(post("/api/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Gold"));
    }

    // ------------------------------------------------------------------ DELETE /api/admin/plans/{id}

    @Test
    @DisplayName("DELETE /api/admin/plans/{id} — 200 con messaggio di conferma")
    void deletePlan_returns200() throws Exception {
        doNothing().when(adminFacade).deletePlan(anyLong());

        mockMvc.perform(delete("/api/admin/plans/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plan deleted successfully"));
    }

    // ------------------------------------------------------------------ GET /api/admin/stats

    @Test
    @DisplayName("GET /api/admin/stats — 200 con statistiche")
    void getStats_returns200() throws Exception {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(50).totalActiveSubscriptions(30).bookingsTotal(200)
                .build();
        when(adminFacade.getAdminStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(50));
    }
}
