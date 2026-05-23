package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.UserFacadeImpl;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.ReviewMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Review;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.*;
import com.project.tesi.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock private UserService userService;
    @Mock private ReviewService reviewService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private ProfessionalStatsService professionalStatsService;
    @Mock private SlotService slotService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private BookingMapper bookingMapper;
    @Mock private ReviewMapper reviewMapper;
    @Mock private UserRepository userRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private PlanRepository planRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    @DisplayName("createBooking — delega al SlotService")
    void createBooking() {
        BookingRequest req = new BookingRequest(10L);
        BookingResponse resp = BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build();
        when(slotService.createBooking(req, 1L)).thenReturn(resp);

        assertThat(userFacade.createBooking(req, 1L)).isEqualTo(resp);
    }

    @Test
    @DisplayName("cancelBooking — delega al SlotService")
    void cancelBooking() {
        userFacade.cancelBooking(10L, 1L);
        verify(slotService).cancelBooking(10L, 1L);
    }

    @Test
    @DisplayName("addReview — verifica unicità, permesso e salvataggio")
    void addReview() {
        User client = User.builder().id(1L).email("client@test.com").password("password123").role(Role.CLIENT).build();
        User professional = User.builder().id(2L).email("pro@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        ReviewRequest req = new ReviewRequest(2L, 5, "test");
        Review savedReview = Review.builder().id(1L).rating(5).client(client).professional(professional).build();
        ReviewResponse expected = ReviewResponse.builder().rating(5).build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(reviewService.canClientReview(1L, 2L)).thenReturn(true);
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(expected);

        assertThat(userFacade.addReview(req, 1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getReviewsForProfessional — delega a reviewService e mappa")
    void getReviewsForProfessional() {
        User professional = User.builder().id(2L).email("pro@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        when(userService.getUserById(2L)).thenReturn(professional);
        when(reviewService.findByProfessional(professional)).thenReturn(List.of());
        when(reviewMapper.toResponseList(List.of())).thenReturn(List.of());

        assertThat(userFacade.getReviewsForProfessional(2L)).isEmpty();
    }

    @Test
    @DisplayName("canClientReview — delega al ReviewService")
    void canClientReview() {
        when(reviewService.canClientReview(1L, 2L)).thenReturn(true);
        assertThat(userFacade.canClientReview(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("hasClientReviewed — delega al ReviewService")
    void hasClientReviewed() {
        when(reviewService.hasClientReviewed(1L, 2L)).thenReturn(false);
        assertThat(userFacade.hasClientReviewed(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("activateSubscription — chiama il service e mappa a DTO")
    void activateSubscription() {
        PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Subscription sub = new Subscription();
        sub.setId(1L);
        SubscriptionResponse expected = SubscriptionResponse.builder().id(1L).build();
        when(subscriptionService.activateSubscription(req, 1L)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(expected);

        assertThat(userFacade.activateSubscription(req, 1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getSubscriptionStatus — chiama il service e mappa a DTO")
    void getSubscriptionStatus() {
        Subscription sub = new Subscription();
        sub.setId(1L);
        sub.setActive(true);
        SubscriptionResponse expected = SubscriptionResponse.builder().active(true).build();
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(expected);

        assertThat(userFacade.getSubscriptionStatus(1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getAvailableSlots — delega al SlotService")
    void getAvailableSlots() {
        when(slotService.getAvailableSlots(2L)).thenReturn(List.of());
        assertThat(userFacade.getAvailableSlots(2L)).isEmpty();
    }

    @Test
    @DisplayName("createSlots — delega al SlotService")
    void createSlots() {
        List<SlotDTO> slots = List.of();
        when(slotService.createSlots(2L, slots)).thenReturn(List.of());
        assertThat(userFacade.createSlots(2L, slots)).isEmpty();
    }

    @Test
    @DisplayName("deleteSlot — delega al SlotService con requesterId")
    void deleteSlot() {
        userFacade.deleteSlot(10L, 2L);
        verify(slotService).deleteSlot(10L, 2L);
    }

    @Test
    @DisplayName("getProfessionalStats — delega al ProfessionalStatsService")
    void getProfessionalStats() {
        ProfessionalStatsResponse stats = new ProfessionalStatsResponse(List.of(), 0, List.of(), 0, 0, 5);
        when(professionalStatsService.getProfessionalStats(2L)).thenReturn(stats);

        assertThat(userFacade.getProfessionalStats(2L)).isEqualTo(stats);
    }

    @Test
    @DisplayName("getAdmin — restituisce il primo admin come ClientBasicInfoResponse")
    void getAdmin() {
        User admin = User.builder().id(1L).firstName("Admin").lastName("System")
                .email("admin@test.com").password("password123").role(Role.ADMIN).build();
        when(userService.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

        ClientBasicInfoResponse result = userFacade.getAdmin();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("getClientsForProfessional — ritorna lista clienti di un PT")
    void getClientsForProfessional() {
        User pt = User.builder().id(2L).email("pt@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        User client = User.builder().id(3L).firstName("Luca").lastName("Bianchi")
                .email("luca@test.com").password("password123").role(Role.CLIENT).build();
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userRepository.findByAssignedPT(pt)).thenReturn(List.of(client));

        List<ClientBasicInfoResponse> result = userFacade.getClientsForProfessional(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("luca@test.com");
    }
}
