package com.project.tesi.service.impl;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.enums.Role;
import com.project.tesi.service.AuthResult;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.security.CustomUserDetailsService;
import com.project.tesi.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("mario@test.com")
                .password("password123")
                .firstName("Mario")
                .lastName("Rossi")
                .role(Role.CLIENT)
                .profilePicture("pic.jpg")
                .build();

        loginRequest = new LoginRequest("mario@test.com", "password123");
    }

    @Test
    @DisplayName("login — autentica, genera JWT e restituisce AuthResponse")
    void login_success() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(testUser));

        AuthResult result = authService.login(loginRequest);

        assertThat(result.token()).isEqualTo("jwt-token-123");
        assertThat(result.user().getId()).isEqualTo(1L);
        assertThat(result.user().getFirstName()).isEqualTo("Mario");
        assertThat(result.user().getEmail()).isEqualTo("mario@test.com");
        assertThat(result.user().getRole()).isEqualTo(Role.CLIENT);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login — credenziali errate lancia BadCredentialsException")
    void login_badCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login — utente non trovato dopo autenticazione lancia ResourceNotFoundException")
    void login_userNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
