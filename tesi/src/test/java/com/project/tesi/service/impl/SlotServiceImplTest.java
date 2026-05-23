package com.project.tesi.service.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.NoActiveSubscriptionException;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.VideoConferenceService;
import com.project.tesi.service.strategy.PersonalTrainerBookingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock private SlotRepository slotRepository;
    @Mock private UserRepository userRepository;
    @Mock private WeeklyScheduleRepository weeklyScheduleRepository;
    @Mock private SlotMapper slotMapper;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private VideoConferenceService videoConferenceService;
    @Mock private ActivityFeedService activityFeedService;
    @Mock private EmailService emailService;

    private SlotServiceImpl slotService;

    private User client;
    private User pt;
    private Slot slot;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).firstName("Luca").lastName("Bianchi").build();
        client = User.builder().email("mario@test.com").password("testpass").role(Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi").assignedPT(pt).build();

        slot = Slot.builder().id(10L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        Plan plan = Plan.builder().name("Plan").duration(com.project.tesi.enums.PlanDuration.ANNUALE).fullPrice(100.0).monthlyInstallmentPrice(10.0).build();
        subscription = Subscription.builder()
                .id(100L).user(client).plan(plan).paymentFrequency(com.project.tesi.enums.PaymentFrequency.UNICA_SOLUZIONE)
                .active(true).currentCreditsPT(5).currentCreditsNutri(3).endDate(LocalDateTime.now().plusMonths(1).toLocalDate()).build();

        slotService = new SlotServiceImpl(
                slotRepository, userRepository, weeklyScheduleRepository, slotMapper,
                subscriptionRepository, bookingMapper, List.of(new PersonalTrainerBookingStrategy()),
                videoConferenceService, activityFeedService, emailService);
    }

    // ─── createBooking ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBooking — prenotazione riuscita con crediti scalati")
    void createBooking_success() {
        BookingRequest request = new BookingRequest(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(slot));
        when(subscriptionRepository.findByUserAndActiveTrue(client)).thenReturn(Optional.of(subscription));
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> inv.getArgument(0));
        when(videoConferenceService.generateMeetingLink(any(), any(), any())).thenReturn("https://meet.jit.si/test");

        BookingResponse expectedResp = BookingResponse.builder().id(10L).status(BookingStatus.CONFIRMED).build();
        when(bookingMapper.toResponse(any(Slot.class))).thenReturn(expectedResp);

        BookingResponse result = slotService.createBooking(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(slot.getBookedBy()).isNotNull();

        verify(slotRepository).save(any(Slot.class));
        verify(emailService, times(2)).sendBookingConfirmationEmail(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("createBooking — utente non trovato lancia ResourceNotFoundException")
    void createBooking_userNotFound() {
        BookingRequest request = new BookingRequest(10L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.createBooking(request, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createBooking — slot non trovato lancia ResourceNotFoundException")
    void createBooking_slotNotFound() {
        BookingRequest request = new BookingRequest(999L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(slotRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.createBooking(request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createBooking — slot già prenotato lancia SlotAlreadyBookedException")
    void createBooking_slotAlreadyBooked() {
        slot.setBookedBy(client);
        BookingRequest request = new BookingRequest(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> slotService.createBooking(request, 1L))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    @DisplayName("createBooking — nessun abbonamento attivo lancia NoActiveSubscriptionException")
    void createBooking_noSubscription() {
        BookingRequest request = new BookingRequest(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(slot));
        when(subscriptionRepository.findByUserAndActiveTrue(client)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.createBooking(request, 1L))
                .isInstanceOf(NoActiveSubscriptionException.class);
    }

    @Test
    @DisplayName("createBooking — ruolo professionista non supportato lancia IllegalStateException")
    void createBooking_unsupportedRole() {
        User admin = User.builder().email("admin@test.com").password("testpass").role(Role.ADMIN).id(3L).build();
        Slot adminSlot = Slot.builder().id(20L).professional(admin)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1)).build();

        BookingRequest request = new BookingRequest(20L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(slotRepository.findByIdWithLock(20L)).thenReturn(Optional.of(adminSlot));

        assertThatThrownBy(() -> slotService.createBooking(request, 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    // ─── createSlots ──────────────────────────────────────────────────────────

    @Test @DisplayName("createSlots — crea e salva slot")
    void createSlots_success() {
        SlotDTO dto = SlotDTO.builder()
                .startTime(LocalDateTime.of(2026, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 3, 15, 10, 30)).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        Slot saved = Slot.builder().id(1L).professional(pt)
                .startTime(dto.getStartTime()).endTime(dto.getEndTime()).build();
        when(slotRepository.saveAll(anyList())).thenReturn(List.of(saved));
        when(slotMapper.toDto(saved)).thenReturn(
                SlotDTO.builder().id(1L).startTime(dto.getStartTime()).endTime(dto.getEndTime()).isAvailable(true).professionalId(2L).build());

        List<SlotDTO> result = slotService.createSlots(2L, List.of(dto));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    @Test @DisplayName("createSlots — professionista non trovato")
    void createSlots_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.createSlots(999L, List.of())).isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getAvailableSlots ────────────────────────────────────────────────────

    @Test @DisplayName("getAvailableSlots — restituisce slot disponibili")
    void getAvailableSlots_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        Slot s = Slot.builder().id(1L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .build();
        when(slotRepository.findByProfessionalAndBookedByIsNull(pt)).thenReturn(List.of(s));
        when(slotMapper.toDto(s)).thenReturn(SlotDTO.builder().id(1L).isAvailable(true).professionalId(2L).build());

        List<SlotDTO> result = slotService.getAvailableSlots(2L);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getAvailableSlots — professionista non trovato")
    void getAvailableSlots_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.getAvailableSlots(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteSlot ───────────────────────────────────────────────────────────

    @Test @DisplayName("deleteSlot — chiama deleteById (singolo argomento)")
    void deleteSlot() {
        when(slotRepository.existsById(10L)).thenReturn(true);
        slotService.deleteSlot(10L);
        verify(slotRepository).deleteById(10L);
    }

    // ─── generateSlotsFromSchedule ────────────────────────────────────────────

    @Test @DisplayName("generateSlotsFromSchedule — genera slot da orario settimanale")
    void generateSlotsFromSchedule_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));

        LocalDate nextMonday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        WeeklySchedule schedule = WeeklySchedule.builder()
                .professional(pt).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).build();
        when(weeklyScheduleRepository.findByProfessional(pt)).thenReturn(List.of(schedule));
        when(slotRepository.existsByProfessionalAndStartTime(any(), any())).thenReturn(false);

        slotService.generateSlotsFromSchedule(2L, nextMonday, nextMonday);

        verify(slotRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test @DisplayName("generateSlotsFromSchedule — slot già esistente non viene duplicato")
    void generateSlotsFromSchedule_noDuplicates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        LocalDate nextMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        WeeklySchedule schedule = WeeklySchedule.builder()
                .professional(pt).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        when(weeklyScheduleRepository.findByProfessional(pt)).thenReturn(List.of(schedule));
        when(slotRepository.existsByProfessionalAndStartTime(any(), any())).thenReturn(true);

        slotService.generateSlotsFromSchedule(2L, nextMon, nextMon);

        verify(slotRepository, never()).saveAll(any());
    }
}
