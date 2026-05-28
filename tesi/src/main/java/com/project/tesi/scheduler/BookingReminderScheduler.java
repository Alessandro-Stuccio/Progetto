package com.project.tesi.scheduler;

import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler che invia promemoria email per appuntamenti imminenti.
 * Eseguito ogni 5 minuti (cron configurabile via {@code schedule.time.bookings}).
 * Usa il flag {@code reminderSent} sullo slot per evitare invii duplicati.
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

    /**
     * Trova gli slot in stato {@code CONFIRMED} nei prossimi 35 minuti senza
     * reminder già inviato, spedisce email sia al cliente sia al professionista
     * e imposta {@code reminderSent=true} per prevenire invii duplicati.
     */
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
