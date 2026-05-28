package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.DocumentType;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ProfessionalFacade;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.WeeklyScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione di {@link com.project.tesi.facade.ProfessionalFacade}.
 * Gestisce slot, prenotazioni e statistiche operative dei professionisti
 * (Personal Trainer e Nutrizionisti).
 */
@Component
public class ProfessionalFacadeImpl implements ProfessionalFacade {

    private final UserService userService;
    private final SlotService slotService;
    private final WeeklyScheduleService weeklyScheduleService;
    private final DocumentService documentService;
    private final SlotMapper slotMapper;
    private final BookingMapper bookingMapper;

    public ProfessionalFacadeImpl(UserService userService,
                                   SlotService slotService,
                                   WeeklyScheduleService weeklyScheduleService,
                                   DocumentService documentService,
                                   SlotMapper slotMapper,
                                   BookingMapper bookingMapper) {
        this.userService = userService;
        this.slotService = slotService;
        this.weeklyScheduleService = weeklyScheduleService;
        this.documentService = documentService;
        this.slotMapper = slotMapper;
        this.bookingMapper = bookingMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return slotMapper.toDtoList(slotService.getAvailableSlots(professional));
    }

    @Override
    @Transactional
    public List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots) {
        User professional = userService.getUserById(professionalId);
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono creare slot");
        }
        List<Slot> entities = slotMapper.toEntityList(slots, professional);
        return slotMapper.toDtoList(slotService.createSlots(entities));
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long requesterId) {
        Slot slot = slotService.getSlot(slotId);
        if (!slot.getProfessional().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo slot");
        }
        if (slot.getBookedBy() != null || slot.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Non puoi eliminare uno slot già prenotato.");
        }
        slotService.deleteSlot(slotId);
    }

    /**
     * Genera automaticamente slot da 30 minuti a partire dagli orari settimanali del professionista.
     * Itera ogni giorno nel range {@code startDate}–{@code endDate}, ricerca le regole
     * {@link com.project.tesi.model.WeeklySchedule} corrispondenti al giorno della settimana e
     * produce slot consecutivi di 30 minuti tra {@code startTime} ed {@code endTime}.
     * Gli slot già esistenti (verificati con {@link com.project.tesi.service.SlotService#slotExists})
     * vengono saltati per evitare duplicati.
     *
     * @param professional professionista per cui generare gli slot
     * @param startDate    primo giorno del range (inclusivo)
     * @param endDate      ultimo giorno del range (inclusivo)
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se l'utente non è un professionista
     */
    @Override
    @Transactional
    public void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate) {
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono generare slot");
        }

        List<WeeklySchedule> schedules = weeklyScheduleService.findByProfessional(professional);
        List<Slot> newSlots = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDay = date;

            List<WeeklySchedule> dailyRules = schedules.stream()
                    .filter(s -> s.getDayOfWeek().equals(currentDay.getDayOfWeek()))
                    .toList();

            for (WeeklySchedule rule : dailyRules) {
                LocalTime currentTime = rule.getStartTime();

                while (currentTime.plusMinutes(30).isBefore(rule.getEndTime()) ||
                        currentTime.plusMinutes(30).equals(rule.getEndTime())) {

                    LocalDateTime startSlot = LocalDateTime.of(currentDay, currentTime);
                    LocalDateTime endSlot = startSlot.plusMinutes(30);

                    if (!slotService.slotExists(professional, startSlot)) {
                        newSlots.add(Slot.builder()
                                .professional(professional)
                                .startTime(startSlot)
                                .endTime(endSlot)
                                .bookedBy(null)
                                .build());
                    }

                    currentTime = currentTime.plusMinutes(30);
                }
            }
        }

        if (!newSlots.isEmpty()) {
            slotService.createSlots(newSlots);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono accedere agli appuntamenti.");
        }
        LocalDateTime now = LocalDateTime.now();
        return slotService.findBookingsByProfessional(professional).stream()
                .filter(slot -> slot.getStartTime() != null && slot.getStartTime().isAfter(now))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .map(bookingMapper::toResponse)
                .toList();
    }

    /**
     * Calcola e restituisce le statistiche operative del professionista.
     * Aggrega: prenotazioni del giorno corrente con link meeting, clienti che necessitano
     * attenzione (nessun documento del tipo rilevante negli ultimi 7 giorni),
     * documenti caricati nella settimana corrente e totale clienti assegnati.
     *
     * @param professionalId ID del professionista
     * @return DTO con le statistiche aggregate
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se l'utente non è un professionista
     */
    @Override
    @Transactional(readOnly = true)
    public ProfessionalStatsResponse getProfessionalStats(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono accedere alle statistiche.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        List<Slot> todaySlots = slotService.findTodayByProfessional(professional, dayStart, dayEnd);

        List<ProfessionalStatsResponse.TodayBookingItem> todayList = todaySlots.stream().map(s ->
                ProfessionalStatsResponse.TodayBookingItem.builder()
                        .id(s.getId())
                        .clientName(s.getBookedBy() != null ? s.getBookedBy().getFullName() : "")
                        .clientId(s.getBookedBy() != null ? s.getBookedBy().getId() : null)
                        .startTime(s.getStartTime().toLocalTime().toString().substring(0, 5))
                        .endTime(s.getEndTime().toLocalTime().toString().substring(0, 5))
                        .status(s.getStatus() != null ? s.getStatus().name() : "")
                        .meetingLink(s.getMeetingLink())
                        .build()
        ).collect(Collectors.toList());

        DocumentType relevantDocType = professional.getRole() == Role.PERSONAL_TRAINER
                ? DocumentType.WORKOUT_PLAN : DocumentType.DIET_PLAN;
        List<User> clients = professional.getRole() == Role.PERSONAL_TRAINER
                ? userService.findByAssignedPT(professional)
                : userService.findByAssignedNutritionist(professional);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ProfessionalStatsResponse.ClientAttentionItem> clientsNeedingAttention = new ArrayList<>();
        for (User client : clients) {
            Document latestDoc = documentService.findLatestByOwnerAndType(client, relevantDocType);
            boolean needsAttention = (latestDoc == null || latestDoc.getUploadDate().isBefore(sevenDaysAgo));
            if (needsAttention) {
                clientsNeedingAttention.add(ProfessionalStatsResponse.ClientAttentionItem.builder()
                        .id(client.getId())
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .lastDocDate(latestDoc != null ? latestDoc.getUploadDate().toString() : null)
                        .daysSinceLastDoc(latestDoc != null
                                ? Duration.between(latestDoc.getUploadDate(), LocalDateTime.now()).toDays()
                                : -1)
                        .build());
            }
        }

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int docsUploadedThisWeek = documentService.countUploadedSince(professional, startOfWeek.atStartOfDay());

        return ProfessionalStatsResponse.builder()
                .todayBookings(todayList)
                .todayBookingsCount(todayList.size())
                .clientsNeedingAttention(clientsNeedingAttention)
                .clientsNeedingAttentionCount(clientsNeedingAttention.size())
                .docsUploadedThisWeek(docsUploadedThisWeek)
                .totalClients(clients.size())
                .build();
    }
}
