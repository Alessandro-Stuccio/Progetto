package com.project.tesi.facade;

import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.ReviewResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.review.ReviewNotAllowedException;
import com.project.tesi.facade.impl.ReviewFacadeImpl;
import com.project.tesi.mapper.ReviewMapper;
import com.project.tesi.model.Review;
import com.project.tesi.model.User;
import com.project.tesi.service.ReviewService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewFacadeTest {

    @Mock private UserService userService;
    @Mock private ReviewService reviewService;
    @Mock private SlotService slotService;
    @Mock private ReviewMapper reviewMapper;

    private ReviewFacadeImpl reviewFacade;

    @BeforeEach
    void setUp() {
        reviewFacade = new ReviewFacadeImpl(userService, reviewService, slotService, reviewMapper);
    }

    private User buildUser(Long id, Role role) {
        return User.builder().id(id).email(role.name().toLowerCase() + id + "@test.com")
                .password("password123").firstName("Test").lastName("User").role(role).build();
    }

    @Test
    @DisplayName("addReview — utenti caricati, unicità ok, permesso ok: recensione salvata e mappata")
    void addReview_success() {
        User client = buildUser(1L, Role.CLIENT);
        User professional = buildUser(2L, Role.PERSONAL_TRAINER);
        ReviewRequest req = new ReviewRequest(2L, 5, "ottimo!");
        Review savedReview = Review.builder().id(1L).rating(5).client(client).professional(professional).build();
        ReviewResponse expected = ReviewResponse.builder().rating(5).authorName("Test").build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(true);
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(expected);

        ReviewResponse result = reviewFacade.addReview(req, 1L);

        assertThat(result).isEqualTo(expected);
        verify(reviewService).save(any(Review.class));
    }

    @Test
    @DisplayName("addReview — recensione già presente: lancia ResourceAlreadyExistsException")
    void addReview_duplicateReview() {
        User client = buildUser(1L, Role.CLIENT);
        User professional = buildUser(2L, Role.PERSONAL_TRAINER);

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewFacade.addReview(new ReviewRequest(2L, 4, null), 1L))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(reviewService, never()).save(any());
    }

    @Test
    @DisplayName("addReview — nessun rapporto formale: lancia ReviewNotAllowedException")
    void addReview_notAllowed() {
        User client = buildUser(1L, Role.CLIENT);
        User professional = buildUser(2L, Role.PERSONAL_TRAINER);

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> reviewFacade.addReview(new ReviewRequest(2L, 3, null), 1L))
                .isInstanceOf(ReviewNotAllowedException.class);
        verify(reviewService, never()).save(any());
    }

    @Test
    @DisplayName("getReviewsForProfessional — professional caricato, lista recuperata e mappata")
    void getReviewsForProfessional() {
        User professional = buildUser(2L, Role.PERSONAL_TRAINER);
        Review review = Review.builder().id(1L).rating(5).client(buildUser(1L, Role.CLIENT)).professional(professional).build();
        ReviewResponse dto = ReviewResponse.builder().rating(5).build();

        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.findByProfessional(professional)).thenReturn(List.of(review));
        when(reviewMapper.toResponseList(List.of(review))).thenReturn(List.of(dto));

        List<ReviewResponse> result = reviewFacade.getReviewsForProfessional(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("canClientReview — true tramite relazione di prenotazione")
    void canClientReview() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(true);
        assertThat(reviewFacade.canClientReview(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("hasClientReviewed — delega a existsByClientAndProfessional")
    void hasClientReviewed() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        assertThat(reviewFacade.hasClientReviewed(1L, 2L)).isFalse();
    }
}
