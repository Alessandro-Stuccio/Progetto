package com.project.tesi.service.impl;

import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User pt;
    private User nutri;

    @BeforeEach
    void setUp() {
        pt = User.builder().id(2L).firstName("Luca").lastName("Bianchi").email("pt@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        nutri = User.builder().id(3L).firstName("Sara").lastName("Verdi").email("nutri@test.com").password("password123").role(Role.NUTRITIONIST).build();
        user = User.builder().id(1L).firstName("Mario").lastName("Rossi").email("mario@test.com").password("password123").role(Role.CLIENT).build();
    }

    @Test
    @DisplayName("getUserById — trovato restituisce l'entità")
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThat(userService.getUserById(1L)).isEqualTo(user);
    }

    @Test
    @DisplayName("getUserById — non trovato lancia ResourceNotFoundException")
    void getUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUserByEmail — trovato restituisce l'entità")
    void getUserByEmail_found() {
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        assertThat(userService.getUserByEmail("mario@test.com")).isEqualTo(user);
    }

    @Test
    @DisplayName("getUserByEmail — non trovato lancia ResourceNotFoundException")
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserByEmail("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("existsByEmail — email presente restituisce true")
    void existsByEmail_true() {
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        assertThat(userService.existsByEmail("mario@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail — email assente restituisce false")
    void existsByEmail_false() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThat(userService.existsByEmail("unknown@test.com")).isFalse();
    }

    @Test
    @DisplayName("save — delega a userRepository.save e restituisce l'entità")
    void save_delegates() {
        when(userRepository.save(user)).thenReturn(user);
        assertThat(userService.save(user)).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("findByRole — delega a userRepository.findByRole")
    void findByRole_delegates() {
        when(userRepository.findByRole(Role.CLIENT)).thenReturn(List.of(user));
        List<User> result = userService.findByRole(Role.CLIENT);
        assertThat(result).containsExactly(user);
    }

    @Test
    @DisplayName("findAll — delega a userRepository.findAll")
    void findAll_delegates() {
        when(userRepository.findAll()).thenReturn(List.of(user, pt, nutri));
        assertThat(userService.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("countByAssignedPT — delega a userRepository")
    void countByAssignedPT_delegates() {
        when(userRepository.countByAssignedPT(pt)).thenReturn(7L);
        assertThat(userService.countByAssignedPT(pt)).isEqualTo(7L);
    }

    @Test
    @DisplayName("countByAssignedNutritionist — delega a userRepository")
    void countByAssignedNutritionist_delegates() {
        when(userRepository.countByAssignedNutritionist(nutri)).thenReturn(3L);
        assertThat(userService.countByAssignedNutritionist(nutri)).isEqualTo(3L);
    }
}
