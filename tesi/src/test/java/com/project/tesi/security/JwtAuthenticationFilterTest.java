package com.project.tesi.security;

import com.project.tesi.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitari per {@link JwtAuthenticationFilter}.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal — nessun header Authorization, passa senza autenticare")
    void noAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal — header senza Bearer prefix, passa senza autenticare")
    void noBearerPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal — token valido, imposta autenticazione nel contesto")
    void validToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token");
        when(jwtUtil.extractUsername("valid-jwt-token")).thenReturn("mario@test.com");

        com.project.tesi.model.User userDetails = com.project.tesi.model.User.builder()
                .id(1L).email("mario@test.com").password("password123").role(Role.CLIENT).build();
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("valid-jwt-token", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("doFilterInternal — token non valido, non imposta autenticazione")
    void invalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtil.extractUsername("invalid-token")).thenReturn("mario@test.com");

        com.project.tesi.model.User userDetails = com.project.tesi.model.User.builder()
                .id(1L).email("mario@test.com").password("password123").role(Role.CLIENT).build();
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal — eccezione JWT, risponde 401 senza autenticazione")
    void jwtException() throws Exception {
        PrintWriter writer = mock(PrintWriter.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        when(jwtUtil.extractUsername("malformed-token")).thenThrow(new RuntimeException("JWT malformato"));
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal — email null non imposta autenticazione")
    void nullEmail() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(jwtUtil.extractUsername("some-token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}

