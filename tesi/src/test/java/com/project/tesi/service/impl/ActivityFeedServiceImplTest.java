package com.project.tesi.service.impl;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.SlotRepository;
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

    @Mock private SlotRepository slotRepository;

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
        Slot slot = Slot.builder().id(1L).professional(pt).bookedBy(client)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();

        activityFeedService.logBookingCreated(slot);

        assertThat(slot.getBookedAt()).isNotNull();
        verify(slotRepository).save(slot);
    }

    @Test
    @DisplayName("logBookingCreated — non sovrascrive bookedAt già presente")
    void logBookingCreated_doesNotOverwrite() {
        LocalDateTime existing = LocalDateTime.now().minusHours(1);
        Slot slot = Slot.builder().id(2L).professional(pt).bookedBy(client)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30))
                .bookedAt(existing).build();

        activityFeedService.logBookingCreated(slot);

        assertThat(slot.getBookedAt()).isEqualTo(existing);
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("logDocumentUploaded — non lancia eccezioni")
    void logDocumentUploaded_noException() {
        activityFeedService.logDocumentUploaded(1L, 2L, "WORKOUT_PLAN");
    }
}
