package com.project.tesi.security;

import com.project.tesi.enums.Role;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername — utente trovato restituisce UserDetails corretto")
    void loadUserByUsername_success() {
        User user = User.builder().id(1L).email("mario@test.com").password("testpass").role(Role.CLIENT).build();
        when(userRepository.findByEmailAndDeletedFalse("mario@test.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.getUserDetails().loadUserByUsername("mario@test.com");

        assertThat(details.getUsername()).isEqualTo("mario@test.com");
        assertThat(details.getPassword()).isEqualTo("testpass");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CLIENT");
    }

    @Test
    @DisplayName("loadUserByUsername — utente PT restituisce ROLE_PERSONAL_TRAINER")
    void loadUserByUsername_pt() {
        User user = User.builder().id(2L).email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
        when(userRepository.findByEmailAndDeletedFalse("pt@test.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.getUserDetails().loadUserByUsername("pt@test.com");

        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_PERSONAL_TRAINER");
    }

    @Test
    @DisplayName("loadUserByUsername — utente non trovato lancia UsernameNotFoundException")
    void loadUserByUsername_notFound() {
        when(userRepository.findByEmailAndDeletedFalse("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.getUserDetails().loadUserByUsername("nobody@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nobody@test.com");
    }
}
