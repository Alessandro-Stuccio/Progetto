package com.project.tesi.service.impl;

import com.project.tesi.enums.BookingStatus;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione di {@link SlotService}.
 * Gestisce il ciclo di vita degli slot con locking pessimistico per-slot tramite
 * {@code ConcurrentHashMap<Long, ReentrantLock>} per prevenire il double-booking concorrente.
 * Usa {@link com.project.tesi.repository.SlotRepository} con
 * {@code @Lock(PESSIMISTIC_WRITE)} per le operazioni critiche.
 */
@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    private final SlotRepository slotRepository;

    public SlotServiceImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public List<Slot> createSlots(List<Slot> slots) {
        return slotRepository.saveAll(slots);
    }

    @Override
    public List<Slot> getAvailableSlots(User professional) {
        return slotRepository.findByProfessionalAndBookedByIsNull(professional);
    }

    @Override
    public Slot getSlot(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));
    }

    /**
     * Acquisisce il lock pessimistico per-slot tramite {@code findByIdWithLock}, verifica
     * la disponibilità dello slot, imposta l'utente prenotante, lo stato {@code CONFIRMED},
     * il link Jitsi e il timestamp {@code bookedAt}.
     * Lancia {@link com.project.tesi.exception.booking.SlotAlreadyBookedException} se lo slot
     * è già occupato, o {@link com.project.tesi.exception.concurrent.ConcurrentUpdateException}
     * su conflitto di versione ottimistica.
     *
     * @param slotId      id dello slot da prenotare
     * @param user        utente che effettua la prenotazione
     * @param meetingLink link Jitsi generato per la videoconferenza
     * @return lo slot aggiornato con i dati della prenotazione
     */
    @Override
    public Slot saveBooking(Long slotId, User user, String meetingLink) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

        if (slot.getBookedBy() != null) {
            throw new SlotAlreadyBookedException("Slot non più disponibile");
        }

        slot.setBookedBy(user);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink(meetingLink);
        slot.setBookedAt(LocalDateTime.now());
        return slotRepository.save(slot);
    }

    @Override
    public void deleteSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Slot", slotId);
        }
        slotRepository.deleteById(slotId);
    }

    /**
     * Annulla la prenotazione dello slot indicato: azzera {@code bookedBy}, {@code meetingLink}
     * e {@code bookedAt}, imposta lo stato a {@code CANCELED}.
     * I crediti vengono ripristinati dal livello superiore (facade) prima di invocare questo metodo.
     *
     * @param slotId id dello slot da cancellare
     * @param userId id dell'utente proprietario della prenotazione (usato per validazione esterna)
     */
    @Override
    public void cancelBooking(Long slotId, Long userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione", slotId));
        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CANCELED);
        slot.setMeetingLink(null);
        slot.setBookedAt(null);
        slotRepository.save(slot);
    }

    @Override
    public List<Slot> findRecentByUser(User user, LocalDateTime since) {
        return slotRepository.findRecentByBookedBy(user, since);
    }

    @Override
    public List<Slot> findRecentByProfessional(User professional, LocalDateTime since) {
        return slotRepository.findRecentByProfessional(professional, since);
    }

    @Override
    public List<Slot> findBookingsByProfessional(User professional) {
        return slotRepository.findByProfessional(professional).stream()
                .filter(s -> s.getBookedBy() != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Slot> findFutureByUser(User user, LocalDateTime from) {
        return slotRepository.findFutureByBookedBy(user, from);
    }

    @Override
    public boolean slotExists(User professional, LocalDateTime startTime) {
        return slotRepository.existsByProfessionalAndStartTime(professional, startTime);
    }

    @Override
    public List<Slot> getAllBookedSlots() {
        return slotRepository.findAllBooked();
    }

    /**
     * Logga a livello INFO i dettagli della prenotazione appena creata.
     * Se {@code bookedAt} non è ancora impostato, lo valorizza con il timestamp corrente
     * e persiste lo slot prima di emettere il log (pattern Observer per l'activity feed).
     *
     * @param slot lo slot appena prenotato
     */
    @Override
    public void logBookingCreated(Slot slot) {
        if (slot.getBookedAt() == null) {
            slot.setBookedAt(LocalDateTime.now());
            slotRepository.save(slot);
            log.info("ActivityFeed [Observer]: timestamp bookedAt registrato per slot ID={}", slot.getId());
        } else {
            log.info("ActivityFeed [Observer]: slot ID={} già registrato (bookedAt={}).", slot.getId(), slot.getBookedAt());
        }
    }

    @Override
    public List<Slot> findTodayByProfessional(User professional, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return slotRepository.findTodayByProfessional(professional, dayStart, dayEnd);
    }

    @Override
    public boolean hasBookingBetween(Long clientId, Long professionalId) {
        return slotRepository.existsByBookedByIdAndProfessionalId(clientId, professionalId);
    }
}
