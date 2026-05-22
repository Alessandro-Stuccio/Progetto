package com.project.tesi.service.impl;

import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityFeedServiceImplTest {

    @Mock private BookingRepository bookingRepository;

    @InjectMocks private ActivityFeedServiceImpl activityFeedService;

    private User client;
    private User pt;

    @BeforeEach
    void setUp() {
        pt = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).build();
        client = User.builder().email("mario@test.com").password("testpass").role(Role.CLIENT).id(1L).build();
    }

    @Test
    @DisplayName("logBookingCreated — imposta bookedAt se assente e salva")
    void logBookingCreated_setsBookedAt() {
        Slot slot = Slot.builder().professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();
        Booking booking = Booking.builder().id(1L).user(client).slot(slot).build();

        activityFeedService.logBookingCreated(booking);

        assertThat(booking.getBookedAt()).isNotNull();
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("logBookingCreated — non sovrascrive bookedAt già presente")
    void logBookingCreated_doesNotOverwrite() {
        Slot slot = Slot.builder().professional(pt)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();
        LocalDateTime existing = LocalDateTime.now().minusHours(1);
        Booking booking = Booking.builder().id(2L).user(client).slot(slot).bookedAt(existing).build();

        activityFeedService.logBookingCreated(booking);

        assertThat(booking.getBookedAt()).isEqualTo(existing);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("logDocumentUploaded — non lancia eccezioni")
    void logDocumentUploaded_noException() {
        activityFeedService.logDocumentUploaded(1L, 2L, "WORKOUT_PLAN");
    }
}
