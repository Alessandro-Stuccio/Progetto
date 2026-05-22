package com.project.tesi.scheduler;

import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingReminderScheduler.class);

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final EmailService emailService;

    public BookingReminderScheduler(BookingRepository bookingRepository,
                                    SlotRepository slotRepository,
                                    EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${schedule.time.bookings}")
    @Transactional
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(35);

        List<Booking> upcoming = bookingRepository.findUpcomingNeedingReminder(now, windowEnd);

        if (upcoming.isEmpty()) return;

        log.info("Trovate {} prenotazioni imminenti da notificare", upcoming.size());

        for (Booking booking : upcoming) {
            try {
                User client = booking.getUser();
                Slot slot = booking.getSlot();
                User professional = slot.getProfessional();
                LocalDateTime startTime = slot.getStartTime();
                String meetingLink = slot.getMeetingLink();

                String clientName = client.getFullName();
                String profName = professional.getFullName();

                emailService.sendBookingReminderEmail(
                        client.getEmail(),
                        client.getFirstName(),
                        profName,
                        startTime,
                        meetingLink,
                        true
                );

                emailService.sendBookingReminderEmail(
                        professional.getEmail(),
                        professional.getFirstName(),
                        clientName,
                        startTime,
                        meetingLink,
                        false
                );

                slot.setReminderSent(true);
                slotRepository.save(slot);

                log.info("Promemoria inviato per booking #{} — {} con {}",
                        booking.getId(), clientName, profName);

            } catch (Exception e) {
                log.error("Errore nell'invio del promemoria per booking #{}: {}",
                        booking.getId(), e.getMessage());
            }
        }
    }
}
