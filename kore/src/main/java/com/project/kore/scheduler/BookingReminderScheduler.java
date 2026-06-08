package com.project.kore.scheduler;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.repository.SlotRepository;
import com.project.kore.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Invia i promemoria email per gli appuntamenti imminenti. Gira ogni 5 minuti
 * (cron in {@code schedule.time.bookings}) e usa il flag {@code reminderSent}
 * sullo slot per non mandare lo stesso avviso due volte.
 */
@Component
public class BookingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingReminderScheduler.class);

    private final SlotRepository slotRepository;
    private final EmailService emailService;

    public BookingReminderScheduler(SlotRepository slotRepository, EmailService emailService) {
        this.slotRepository = slotRepository;
        this.emailService = emailService;
    }

    // Prende gli slot confermati nei prossimi 35 minuti senza reminder, avvisa via
    // email cliente e professionista e segna reminderSent per non ripetere l'invio.
    @Scheduled(cron = "${schedule.time.bookings}")
    @Transactional
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(35);

        List<Slot> upcoming = slotRepository.findUpcomingNeedingReminder(now, windowEnd);

        if (upcoming.isEmpty()) return;

        log.info("Trovate {} prenotazioni imminenti da notificare", upcoming.size());

        for (Slot slot : upcoming) {
            try {
                User client = slot.getBookedBy();
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

                log.info("Promemoria inviato per slot #{} — {} con {}", slot.getId(), clientName, profName);

            } catch (Exception e) {
                log.error("Errore nell'invio del promemoria per slot #{}: {}", slot.getId(), e.getMessage());
            }
        }
    }
}
