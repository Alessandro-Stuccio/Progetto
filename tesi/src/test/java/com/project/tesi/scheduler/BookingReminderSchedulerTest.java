package com.project.tesi.scheduler;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.exception.email.EmailDeliveryException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingReminderSchedulerTest {

    @Mock private SlotRepository slotRepository;
    @Mock private EmailService emailService;

    private BookingReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new BookingReminderScheduler(slotRepository, emailService);
    }

    @Test
    @DisplayName("sendBookingReminders — invia due email e marca reminderSent sullo slot")
    void sendBookingReminders_success() {
        Slot slot = buildSlot(1L);
        when(slotRepository.findUpcomingNeedingReminder(any(), any())).thenReturn(List.of(slot));

        scheduler.sendBookingReminders();

        verify(emailService, times(2)).sendBookingReminderEmail(anyString(), anyString(), anyString(), any(), anyString(), anyBoolean());
        ArgumentCaptor<Slot> savedSlot = ArgumentCaptor.forClass(Slot.class);
        verify(slotRepository).save(savedSlot.capture());
        assertThat(savedSlot.getValue().isReminderSent()).isTrue();
    }

    @Test
    @DisplayName("sendBookingReminders — se invio fallisce non marca reminderSent")
    void sendBookingReminders_failureDoesNotMarkAsSent() {
        Slot slot = buildSlot(2L);
        when(slotRepository.findUpcomingNeedingReminder(any(), any())).thenReturn(List.of(slot));
        doThrow(new EmailDeliveryException("provider down"))
                .when(emailService)
                .sendBookingReminderEmail(anyString(), anyString(), anyString(), any(), anyString(), anyBoolean());

        scheduler.sendBookingReminders();

        verify(slotRepository, never()).save(any());
        assertThat(slot.isReminderSent()).isFalse();
    }

    private Slot buildSlot(Long id) {
        User client = User.builder().email("mario@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).firstName("Mario").lastName("Rossi").build();
        User professional = User.builder().email("luca@test.com").password("testpass").role(com.project.tesi.enums.Role.PERSONAL_TRAINER).firstName("Luca").lastName("Bianchi").build();

        Slot slot = Slot.builder()
                .id(id)
                .professional(professional)
                .startTime(LocalDateTime.now().plusMinutes(30))
                .endTime(LocalDateTime.now().plusMinutes(90))
                .build();
        slot.setBookedBy(client);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink("https://meet.jit.si/test-room");
        slot.setReminderSent(false);
        return slot;
    }
}
