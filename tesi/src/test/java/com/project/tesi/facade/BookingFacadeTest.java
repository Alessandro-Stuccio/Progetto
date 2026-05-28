package com.project.tesi.facade;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.BookingFacadeImpl;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.*;
import com.project.tesi.service.strategy.BookingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingFacadeTest {

    @Mock private UserService userService;
    @Mock private SlotService slotService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private VideoConferenceService videoConferenceService;
    @Mock private EmailService emailService;
    @Mock private BookingStrategy ptStrategy;
    @Mock private BookingMapper bookingMapper;

    private BookingFacadeImpl bookingFacade;

    @BeforeEach
    void setUp() {
        bookingFacade = new BookingFacadeImpl(userService, slotService,
                subscriptionService, videoConferenceService, emailService, List.of(ptStrategy), bookingMapper);
    }

    private User buildUser(Long id, Role role) {
        return User.builder().id(id).email(role.name().toLowerCase() + id + "@test.com")
                .password("password123").firstName("Test").lastName("User").role(role).build();
    }

    @Test
    @DisplayName("createBooking — slot libero, strategia applicata, prenotazione salvata e risposta restituita")
    void createBooking_success() {
        User client = buildUser(1L, Role.CLIENT);
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Slot slot = Slot.builder().id(10L).professional(pt)
                .startTime(LocalDateTime.now().plusDays(3))
                .endTime(LocalDateTime.now().plusDays(3).plusMinutes(30)).build();
        Slot saved = Slot.builder().id(10L).professional(pt).bookedBy(client)
                .startTime(slot.getStartTime()).endTime(slot.getEndTime()).build();
        Subscription sub = new Subscription();
        sub.setEndDate(LocalDate.now().plusMonths(1));
        BookingResponse expected = BookingResponse.builder().id(10L).build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(10L)).thenReturn(slot);
        when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(sub);
        when(subscriptionService.findActiveByUserWithLock(client)).thenReturn(Optional.of(sub));
        when(subscriptionService.save(any())).thenReturn(sub);
        when(videoConferenceService.generateMeetingLink(client, pt, slot)).thenReturn("https://meet.jit.si/abc");
        when(slotService.saveBooking(10L, client, "https://meet.jit.si/abc")).thenReturn(saved);
        when(bookingMapper.toResponse(saved)).thenReturn(expected);

        BookingResponse result = bookingFacade.createBooking(new BookingRequest(10L), 1L);

        assertThat(result).isEqualTo(expected);
        verify(ptStrategy).consumeCredits(sub);
        verify(slotService).logBookingCreated(saved);
    }

    @Test
    @DisplayName("cancelBooking — slot letto, prenotazione cancellata, crediti rimborsati")
    void cancelBooking_success() {
        User client = buildUser(1L, Role.CLIENT);
        User pt = buildUser(2L, Role.PERSONAL_TRAINER);
        Slot slot = Slot.builder().id(10L).professional(pt).bookedBy(client)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusMinutes(30)).build();
        slot.setStatus(BookingStatus.CONFIRMED);
        Subscription sub = new Subscription();

        when(slotService.getSlot(10L)).thenReturn(slot);
        when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);
        when(subscriptionService.findActiveByUserWithLock(client)).thenReturn(Optional.of(sub));
        when(subscriptionService.save(sub)).thenReturn(sub);

        bookingFacade.cancelBooking(10L, 1L);

        verify(slotService).cancelBooking(10L, 1L);
        verify(ptStrategy).refundCredits(sub);
        verify(subscriptionService).save(sub);
    }
}
