package com.project.tesi.service.impl;

import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.*;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalStatsServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private SlotRepository slotRepository;
    @Mock private DocumentRepository documentRepository;

    @InjectMocks private ProfessionalStatsServiceImpl statsService;

    private User pt, nutri, client;

    @BeforeEach
    void setUp() {
        pt     = User.builder().email("pt@test.com").password("testpass").role(Role.PERSONAL_TRAINER).id(2L).firstName("Luca").lastName("Bianchi").build();
        nutri  = User.builder().email("nutri@test.com").password("testpass").role(Role.NUTRITIONIST).id(3L).firstName("Sara").lastName("Verdi").build();
        client = User.builder().email("mario@test.com").password("testpass").role(Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi").build();
    }

    @Test @DisplayName("getProfessionalStats — PT con booking e clienti")
    void getProfessionalStats_pt() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));

        Slot slot = Slot.builder().professional(pt)
                .startTime(LocalDateTime.now().withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().withHour(10).withMinute(30)).build();
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink("https://meet.jit.si/test");
        slot.setBookedBy(client);
        when(slotRepository.findTodayByProfessional(eq(pt), any(), any())).thenReturn(List.of(slot));
        when(userRepository.findByAssignedPT(pt)).thenReturn(List.of(client));
        Document oldDoc = Document.builder().uploadDate(LocalDateTime.now().minusDays(10)).build();
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.WORKOUT_PLAN)).thenReturn(oldDoc);
        when(documentRepository.countByUploaderSince(eq(pt), any())).thenReturn(3);

        ProfessionalStatsResponse stats = statsService.getProfessionalStats(2L);

        assertThat(stats.todayBookingsCount()).isEqualTo(1);
        assertThat(stats.totalClients()).isEqualTo(1);
        assertThat(stats.docsUploadedThisWeek()).isEqualTo(3);
        assertThat(stats.clientsNeedingAttentionCount()).isEqualTo(1);
    }

    @Test @DisplayName("getProfessionalStats — Nutrizionista senza prenotazioni")
    void getProfessionalStats_nutri() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(nutri));
        when(slotRepository.findTodayByProfessional(eq(nutri), any(), any())).thenReturn(List.of());
        when(userRepository.findByAssignedNutritionist(nutri)).thenReturn(List.of(client));
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN)).thenReturn(null);
        when(documentRepository.countByUploaderSince(eq(nutri), any())).thenReturn(0);

        ProfessionalStatsResponse stats = statsService.getProfessionalStats(3L);

        assertThat(stats.todayBookingsCount()).isEqualTo(0);
        assertThat(stats.clientsNeedingAttentionCount()).isEqualTo(1);
        assertThat(stats.clientsNeedingAttention().get(0).daysSinceLastDoc()).isEqualTo(-1L);
    }

    @Test @DisplayName("getProfessionalStats — professionista non trovato")
    void getProfessionalStats_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> statsService.getProfessionalStats(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("getProfessionalStats — utente non professionista lancia IllegalArgument")
    void getProfessionalStats_notProfessional() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        assertThatThrownBy(() -> statsService.getProfessionalStats(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test @DisplayName("getProfessionalStats — PT con documento recente → no attenzione necessaria")
    void getProfessionalStats_recentDoc() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(pt));
        when(slotRepository.findTodayByProfessional(eq(pt), any(), any())).thenReturn(List.of());
        when(userRepository.findByAssignedPT(pt)).thenReturn(List.of(client));
        Document recentDoc = Document.builder().uploadDate(LocalDateTime.now().minusDays(2)).build();
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.WORKOUT_PLAN)).thenReturn(recentDoc);
        when(documentRepository.countByUploaderSince(eq(pt), any())).thenReturn(1);

        ProfessionalStatsResponse stats = statsService.getProfessionalStats(2L);
        assertThat(stats.clientsNeedingAttentionCount()).isEqualTo(0);
    }
}
