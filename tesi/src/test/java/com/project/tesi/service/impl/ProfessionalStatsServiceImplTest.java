package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test @DisplayName("getTodaySlots — delega a slotRepository.findTodayByProfessional")
    void getTodaySlots() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime end = start.plusDays(1);
        Slot slot = Slot.builder().professional(pt)
                .startTime(start.withHour(10)).endTime(start.withHour(10).withMinute(30))
                .build();
        slot.setStatus(BookingStatus.CONFIRMED);
        when(slotRepository.findTodayByProfessional(eq(pt), any(), any())).thenReturn(List.of(slot));

        List<Slot> result = statsService.getTodaySlots(pt, start, end);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getTodaySlots — lista vuota se nessun slot oggi")
    void getTodaySlots_empty() {
        when(slotRepository.findTodayByProfessional(eq(pt), any(), any())).thenReturn(List.of());
        assertThat(statsService.getTodaySlots(pt, LocalDateTime.now(), LocalDateTime.now().plusDays(1))).isEmpty();
    }

    @Test @DisplayName("getAssignedClients — PT restituisce clienti via findByAssignedPT")
    void getAssignedClients_pt() {
        when(userRepository.findByAssignedPT(pt)).thenReturn(List.of(client));
        List<User> result = statsService.getAssignedClients(pt);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test @DisplayName("getAssignedClients — Nutrizionista restituisce clienti via findByAssignedNutritionist")
    void getAssignedClients_nutri() {
        when(userRepository.findByAssignedNutritionist(nutri)).thenReturn(List.of(client));
        List<User> result = statsService.getAssignedClients(nutri);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("getLatestDocumentByOwnerAndType — delega al documentRepository")
    void getLatestDocumentByOwnerAndType() {
        Document doc = Document.builder().uploadDate(LocalDateTime.now().minusDays(3)).build();
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.WORKOUT_PLAN)).thenReturn(doc);

        Document result = statsService.getLatestDocumentByOwnerAndType(client, DocumentType.WORKOUT_PLAN);
        assertThat(result).isEqualTo(doc);
    }

    @Test @DisplayName("getLatestDocumentByOwnerAndType — null se nessun documento")
    void getLatestDocumentByOwnerAndType_null() {
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN)).thenReturn(null);
        assertThat(statsService.getLatestDocumentByOwnerAndType(client, DocumentType.DIET_PLAN)).isNull();
    }

    @Test @DisplayName("countDocumentsUploadedSince — delega al documentRepository")
    void countDocumentsUploadedSince() {
        when(documentRepository.countByUploaderSince(eq(pt), any())).thenReturn(3);
        assertThat(statsService.countDocumentsUploadedSince(pt, LocalDateTime.now().minusDays(7))).isEqualTo(3);
    }
}
