package com.project.tesi.mapper;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.model.Slot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper per la conversione di {@link Slot} in {@link BookingResponse}.
 */
@Component
public class BookingMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converte uno {@link Slot} in {@link BookingResponse}, formattando data e orari
     * e calcolando il flag {@code canJoin} tramite {@link #isMeetingJoinable}.
     *
     * @param slot lo slot da convertire
     * @return il DTO di risposta, o {@code null} se lo slot è {@code null}
     */
    public BookingResponse toResponse(Slot slot) {
        if (slot == null) return null;

        LocalDateTime start = slot.getStartTime();
        LocalDateTime end = slot.getEndTime();

        return BookingResponse.builder()
                .id(slot.getId())
                .date(start.format(DATE_FORMATTER))
                .startTime(start.format(TIME_FORMATTER))
                .endTime(end.format(TIME_FORMATTER))
                .professionalName(slot.getProfessional().getFullName())
                .clientName(slot.getBookedBy() != null ? slot.getBookedBy().getFullName() : "")
                .professionalRole(slot.getProfessional().getRole())
                .meetingLink(slot.getMeetingLink())
                .status(slot.getStatus())
                .canJoin(isMeetingJoinable(start))
                .build();
    }

    /**
     * Determina se il meeting è accessibile in questo momento.
     * Il meeting è considerato apribile a partire da 10 minuti prima dell'inizio
     * e fino a 30 minuti dopo l'orario di inizio dello slot.
     *
     * @param startTime orario di inizio dello slot
     * @return {@code true} se l'ora corrente rientra nella finestra di accesso
     */
    private boolean isMeetingJoinable(LocalDateTime startTime) {
        if (startTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime.minusMinutes(10)) && !now.isAfter(startTime.plusMinutes(30));
    }
}
