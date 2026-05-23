package com.project.tesi.service.impl;

import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse.ClientAttentionItem;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse.TodayBookingItem;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ProfessionalStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessionalStatsServiceImpl implements ProfessionalStatsService {

    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final DocumentRepository documentRepository;

    public ProfessionalStatsServiceImpl(UserRepository userRepository,
                                         SlotRepository slotRepository,
                                         DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalStatsResponse getProfessionalStats(Long professionalId) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new IllegalArgumentException("L'utente con ID " + professionalId + " non è un professionista.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        List<Slot> todaySlots = slotRepository.findTodayByProfessional(professional, dayStart, dayEnd);

        List<TodayBookingItem> todayList = todaySlots.stream().map(s -> new TodayBookingItem(
                s.getId(),
                s.getBookedBy() != null ? s.getBookedBy().getFullName() : "",
                s.getBookedBy() != null ? s.getBookedBy().getId() : null,
                s.getStartTime().toLocalTime().toString().substring(0, 5),
                s.getEndTime().toLocalTime().toString().substring(0, 5),
                s.getStatus() != null ? s.getStatus().name() : "",
                s.getMeetingLink()
        )).collect(Collectors.toList());

        List<User> clients;
        DocumentType relevantDocType;
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            clients = userRepository.findByAssignedPT(professional);
            relevantDocType = DocumentType.WORKOUT_PLAN;
        } else {
            clients = userRepository.findByAssignedNutritionist(professional);
            relevantDocType = DocumentType.DIET_PLAN;
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ClientAttentionItem> clientsNeedingAttention = new ArrayList<>();
        for (User client : clients) {
            Document latestDoc = documentRepository.findLatestByOwnerAndType(client, relevantDocType);
            boolean needsAttention = (latestDoc == null || latestDoc.getUploadDate().isBefore(sevenDaysAgo));
            if (needsAttention) {
                clientsNeedingAttention.add(new ClientAttentionItem(
                        client.getId(),
                        client.getFirstName(),
                        client.getLastName(),
                        latestDoc != null ? latestDoc.getUploadDate().toString() : null,
                        latestDoc != null
                                ? java.time.Duration.between(latestDoc.getUploadDate(), LocalDateTime.now()).toDays()
                                : -1));
            }
        }

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int docsUploadedThisWeek = documentRepository.countByUploaderSince(professional, startOfWeek.atStartOfDay());

        return new ProfessionalStatsResponse(
                todayList,
                todayList.size(),
                clientsNeedingAttention,
                clientsNeedingAttention.size(),
                docsUploadedThisWeek,
                clients.size());
    }
}
