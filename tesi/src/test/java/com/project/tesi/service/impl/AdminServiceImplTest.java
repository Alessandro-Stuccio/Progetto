package com.project.tesi.service.impl;

import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.*;
import com.project.tesi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private WeeklyScheduleRepository weeklyScheduleRepository;

    @InjectMocks private AdminServiceImpl adminService;

    private User user;
    private Plan plan;

    @BeforeEach
    void setUp() {
        user = User.builder().email("mario@test.com").password("testpass").role(Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi").build();
        plan = Plan.builder().id(1L).name("Premium").duration(PlanDuration.ANNUALE)
                .monthlyCreditsPT(8).monthlyCreditsNutri(4)
                .fullPrice(1200.0).monthlyInstallmentPrice(100.0).build();
    }

    @Test @DisplayName("getAllUsers — restituisce lista utenti")
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> result = adminService.getAllUsers();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("mario@test.com");
    }

    @Test @DisplayName("findUserById — utente trovato")
    void findUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = adminService.findUserById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test @DisplayName("findUserById — utente non trovato lancia ResourceNotFoundException")
    void findUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.findUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("existsUserByEmail — email presente restituisce true")
    void existsUserByEmail_true() {
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        assertThat(adminService.existsUserByEmail("mario@test.com")).isTrue();
    }

    @Test @DisplayName("existsUserByEmail — email assente restituisce false")
    void existsUserByEmail_false() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThat(adminService.existsUserByEmail("unknown@test.com")).isFalse();
    }

    @Test @DisplayName("existsUserByEmailExcluding — email presa da altro utente restituisce true")
    void existsUserByEmailExcluding_conflict() {
        User other = User.builder().id(2L).email("mario@test.com").password("testpass").role(Role.CLIENT).firstName("X").lastName("Y").build();
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(other));
        assertThat(adminService.existsUserByEmailExcluding("mario@test.com", 1L)).isTrue();
    }

    @Test @DisplayName("existsUserByEmailExcluding — email appartiene allo stesso utente restituisce false")
    void existsUserByEmailExcluding_noConflict() {
        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        assertThat(adminService.existsUserByEmailExcluding("mario@test.com", 1L)).isFalse();
    }

    @Test @DisplayName("saveUser — salva e restituisce utente")
    void saveUser_success() {
        when(userRepository.save(user)).thenReturn(user);
        User result = adminService.saveUser(user);
        assertThat(result.getEmail()).isEqualTo("mario@test.com");
        verify(userRepository).save(user);
    }

    @Test @DisplayName("deleteUser — elimina utente")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        adminService.deleteUser(1L);
        verify(userRepository).delete(user);
    }

    @Test @DisplayName("deleteUser — utente non trovato")
    void deleteUser_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.deleteUser(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("deleteUser — elimina solo l'utente (gestione abbonamenti delegata alla facade)")
    void deleteUser_withSubscription() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L);
        verify(userRepository).delete(user);
    }

    @Test @DisplayName("getAllSubscriptions — restituisce lista")
    void getAllSubscriptions() {
        Subscription sub = Subscription.builder().id(1L).user(user).plan(plan).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).startDate(java.time.LocalDate.now()).endDate(java.time.LocalDate.now().plusYears(1)).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        List<Subscription> result = adminService.getAllSubscriptions();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlan().getName()).isEqualTo("Premium");
    }

    @Test @DisplayName("getAllSubscriptions — plan null")
    void getAllSubscriptions_planNull() {
        Plan dummyPlan = Plan.builder().id(0L).name("N/A").duration(PlanDuration.ANNUALE).fullPrice(1.0).monthlyInstallmentPrice(1.0).build();
        Subscription sub = Subscription.builder().id(1L).user(user).plan(dummyPlan).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE)
                .active(false).startDate(null).endDate(null).build();
        sub.setPlan(null);
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        List<Subscription> result = adminService.getAllSubscriptions();
        assertThat(result.get(0).getPlan()).isNull();
    }

}
