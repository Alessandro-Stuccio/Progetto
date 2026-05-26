package com.project.tesi.controller;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.AuthFacade;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private AuthFacade authFacade;
    @Mock private UserFacade userFacade;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("register — chiama UserFacade e restituisce 200 con il profilo creato")
    void register() {
        RegisterRequest req = new RegisterRequest(null, null, "mario@test.com", null, null, null, null, null, null);
        UserResponse userResp = UserResponse.builder().id(1L).email("mario@test.com").role(Role.CLIENT).build();
        when(userFacade.registerUser(req)).thenReturn(userResp);

        ResponseEntity<UserResponse> response = authController.register(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getEmail()).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("ping — restituisce messaggio di health check")
    void ping() {
        ResponseEntity<Map<String, String>> response = authController.ping();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }
}
