package com.project.tesi.mapper;

import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.model.Slot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BookingMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    private boolean isMeetingJoinable(LocalDateTime startTime) {
        if (startTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime.minusMinutes(10)) && !now.isAfter(startTime.plusMinutes(30));
    }
}
