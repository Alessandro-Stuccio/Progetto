package com.project.tesi.scheduler;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.exception.email.EmailDeliveryException;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingReminderSchedulerTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private BookingReminderScheduler scheduler;

    @Test
    @DisplayName("sendBookingReminders - invia due email e marca reminderSent sullo slot")
    void sendBookingReminders_success() {
        Booking booking = buildBooking(1L);
        when(bookingRepository.findUpcomingNeedingReminder(any(), any())).thenReturn(List.of(booking));

        scheduler.sendBookingReminders();

        verify(emailService, times(2)).sendBookingReminderEmail(anyString(), anyString(), anyString(), any(), anyString(), anyBoolean());
        ArgumentCaptor<Slot> savedSlot = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(savedSlot.capture());
        assertThat(savedSlot.getValue().isReminderSent()).isTrue();
    }

    @Test
    @DisplayName("sendBookingReminders - se invio fallisce non marca reminderSent")
    void sendBookingReminders_failureDoesNotMarkAsSent() {
        Booking booking = buildBooking(2L);
        when(bookingRepository.findUpcomingNeedingReminder(any(), any())).thenReturn(List.of(booking));
        doThrow(new EmailDeliveryException("provider down"))
                .when(emailService)
                .sendBookingReminderEmail(anyString(), anyString(), anyString(), any(), anyString(), anyBoolean());

        scheduler.sendBookingReminders();

        verify(slotRepository, never()).save(any());
        assertThat(booking.getSlot().isReminderSent()).isFalse();
    }

    private Booking buildBooking(Long id) {
        User client = User.builder().email("mario@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).firstName("Mario").lastName("Rossi").build();
        User professional = User.builder().email("luca@test.com").password("testpass").role(com.project.tesi.enums.Role.PERSONAL_TRAINER).firstName("Luca").lastName("Bianchi").build();
        Slot slot = Slot.builder()
                .professional(professional)
                .startTime(LocalDateTime.now().plusMinutes(30))
                .endTime(LocalDateTime.now().plusMinutes(90))
                .build();
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink("https://meet.jit.si/test-room");
        slot.setReminderSent(false);

        return Booking.builder()
                .id(id)
                .user(client)
                .slot(slot)
                .build();
    }
}
