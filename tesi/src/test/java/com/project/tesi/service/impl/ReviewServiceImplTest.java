package com.project.tesi.service.impl;

import com.project.tesi.enums.Role;
import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private SlotRepository slotRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User client;
    private User professional;

    @BeforeEach
    void setUp() {
        professional = User.builder().id(2L).firstName("Luca").lastName("Bianchi")
                .email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).build();
        client = User.builder().id(1L).firstName("Mario").lastName("Rossi")
                .email("mario@test.com").password("testpass").role(Role.CLIENT)
                .assignedPT(professional).createdAt(LocalDateTime.now().minusMonths(2)).build();
    }

    @Test
    @DisplayName("save — delega al repository e restituisce la review")
    void save_delegates() {
        Review review = Review.builder().id(1L).client(client).professional(professional)
                .rating(5).comment("Ottimo!").build();
        when(reviewRepository.save(review)).thenReturn(review);

        assertThat(reviewService.save(review)).isEqualTo(review);
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("existsByClientAndProfessional — true quando esiste già")
    void existsByClientAndProfessional_true() {
        when(reviewRepository.existsByClientIdAndProfessionalId(1L, 2L)).thenReturn(true);
        assertThat(reviewService.existsByClientAndProfessional(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("existsByClientAndProfessional — false quando non esiste")
    void existsByClientAndProfessional_false() {
        when(reviewRepository.existsByClientIdAndProfessionalId(1L, 2L)).thenReturn(false);
        assertThat(reviewService.existsByClientAndProfessional(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("findByProfessional — delega al repository")
    void findByProfessional_delegates() {
        Review r = Review.builder().id(1L).client(client).professional(professional).rating(4).build();
        when(reviewRepository.findByProfessional(professional)).thenReturn(List.of(r));

        List<Review> result = reviewService.findByProfessional(professional);
        assertThat(result).hasSize(1).containsExactly(r);
    }

    @Test
    @DisplayName("getAverageRating — restituisce valore medio dal repository")
    void getAverageRating_returnsValue() {
        when(reviewRepository.getAverageRating(2L)).thenReturn(4.5);
        assertThat(reviewService.getAverageRating(2L)).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getAverageRating — null dal repository restituisce 0.0")
    void getAverageRating_nullReturnsZero() {
        when(reviewRepository.getAverageRating(2L)).thenReturn(null);
        assertThat(reviewService.getAverageRating(2L)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("hasBookingRelationship — true quando esiste uno slot prenotato")
    void hasBookingRelationship_true() {
        when(slotRepository.existsByBookedByIdAndProfessionalId(1L, 2L)).thenReturn(true);
        assertThat(reviewService.hasBookingRelationship(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("hasBookingRelationship — false quando nessuno slot prenotato")
    void hasBookingRelationship_false() {
        when(slotRepository.existsByBookedByIdAndProfessionalId(1L, 2L)).thenReturn(false);
        assertThat(reviewService.hasBookingRelationship(1L, 2L)).isFalse();
    }

}
