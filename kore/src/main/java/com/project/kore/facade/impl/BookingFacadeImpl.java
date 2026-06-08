package com.project.kore.facade.impl;

import com.project.kore.dto.request.BookingRequest;
import com.project.kore.dto.response.BookingResponse;
import com.project.kore.enums.BookingStatus;
import com.project.kore.exception.booking.BookingCancellationException;
import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.booking.SubscriptionExpiredException;
import com.project.kore.exception.common.BusinessLogicException;
import com.project.kore.facade.BookingFacade;
import com.project.kore.mapper.BookingMapper;
import com.project.kore.model.Slot;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.*;
import com.project.kore.service.strategy.BookingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordina la prenotazione degli slot. Per evitare il double-booking usa un lock dedicato
 * per ogni slot; la deduzione dei crediti avviene sotto pessimistic write lock sull'abbonamento.
 */
@Component
public class BookingFacadeImpl implements BookingFacade {

    private static final Logger log = LoggerFactory.getLogger(BookingFacadeImpl.class);

    private static class LockReference {
        final ReentrantLock lock = new ReentrantLock();
        int count = 0;
    }

    // Un ReentrantLock per slot, con conteggio dei riferimenti per poterlo rimuovere quando non serve più.
    private final Map<Long, LockReference> slotLocks = new ConcurrentHashMap<>();

    private final UserService userService;
    private final SlotService slotService;
    private final SubscriptionService subscriptionService;
    private final VideoConferenceService videoConferenceService;
    private final EmailService emailService;
    private final List<BookingStrategy> strategies;
    private final BookingMapper bookingMapper;

    public BookingFacadeImpl(UserService userService,
                             SlotService slotService,
                             SubscriptionService subscriptionService,
                             VideoConferenceService videoConferenceService,
                             EmailService emailService,
                             List<BookingStrategy> strategies,
                             BookingMapper bookingMapper) {
        this.userService = userService;
        this.slotService = slotService;
        this.subscriptionService = subscriptionService;
        this.videoConferenceService = videoConferenceService;
        this.emailService = emailService;
        this.strategies = strategies;
        this.bookingMapper = bookingMapper;
    }

    /**
     * Prenota uno slot prendendo prima il lock dedicato a quello slot, così da serializzare
     * i tentativi concorrenti ed evitare il double-booking. Controlla che lo slot sia libero,
     * che non si prenoti con sé stessi e che l'abbonamento copra la data dell'appuntamento;
     * poi genera il link Jitsi, scala i crediti e invia le email di conferma.
     * Un conflitto di optimistic locking sui crediti viene tradotto in IllegalStateException
     * per chiedere di riprovare. Le email sono best-effort: un errore non blocca la prenotazione.
     */
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        Long slotId = request.slotId();
        LockReference ref;
        synchronized (slotLocks) {
            ref = slotLocks.computeIfAbsent(slotId, k -> new LockReference());
            ref.count++;
        }
        ref.lock.lock();
        try {
            User user = userService.getUserById(userId);
            Slot slot = slotService.getSlot(slotId);
            User professional = slot.getProfessional();

            if (user.getId().equals(professional.getId())) {
                throw new BusinessLogicException("Non puoi prenotare con te stesso.");
            }

            if (slot.getBookedBy() != null) {
                throw new SlotAlreadyBookedException("Slot non più disponibile");
            }

            BookingStrategy strategy = strategies.stream()
                    .filter(s -> s.getSupportedRole() == professional.getRole())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Il professionista non è né PT né Nutrizionista"));

            strategy.verifyAssignment(user, professional);

            Subscription sub = subscriptionService.getSubscriptionStatus(user);
            LocalDate today = LocalDate.now();
            if (today.isAfter(sub.getEndDate())) {
                throw new SubscriptionExpiredException(
                        "Impossibile prenotare: il tuo abbonamento è scaduto in data " + sub.getEndDate() + ".");
            }
            if (slot.getStartTime().toLocalDate().isAfter(sub.getEndDate())) {
                throw new SubscriptionExpiredException(
                        "Operazione rifiutata: l'abbonamento scadrà il " + sub.getEndDate() +
                        ", prima della data prevista per questo slot (" + slot.getStartTime().toLocalDate() + ").");
            }

            String meetLink = videoConferenceService.generateMeetingLink(user, professional, slot);
            Slot saved = slotService.saveBooking(slotId, user, meetLink);

            try {
                Subscription activeSub = subscriptionService.findActiveByUserWithLock(user)
                        .orElseThrow(() -> new IllegalStateException("Abbonamento non trovato per l'utente " + user.getId()));
                strategy.consumeCredits(activeSub);
                subscriptionService.save(activeSub);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new IllegalStateException("Aggiornamento crediti fallito per conflitto concorrente. Riprovare.", e);
            }
            slotService.logBookingCreated(saved);

            try {
                emailService.sendBookingConfirmationEmail(user.getEmail(), user.getFirstName(), professional.getFirstName(), saved.getStartTime(), meetLink);
                emailService.sendBookingConfirmationEmail(professional.getEmail(), professional.getFirstName(), user.getFirstName(), saved.getStartTime(), meetLink);
            } catch (Exception e) {
                log.warn("Impossibile inviare email di conferma prenotazione: {}", e.getMessage());
            }

            return bookingMapper.toResponse(saved);
        } finally {
            ref.lock.unlock();
            synchronized (slotLocks) {
                ref.count--;
                if (ref.count == 0) {
                    slotLocks.remove(slotId);
                }
            }
        }
    }

    /**
     * Annulla una prenotazione. Si può annullare solo una propria prenotazione confermata e
     * solo se mancano almeno 24 ore all'appuntamento. Dopo la cancellazione rimborsa il credito
     * tramite la strategy del ruolo e avvisa via email entrambe le parti (invio best-effort).
     */
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Slot slot = slotService.getSlot(bookingId);
        User client = slot.getBookedBy();
        User professional = slot.getProfessional();
        LocalDateTime start = slot.getStartTime();

        if (client == null || !client.getId().equals(userId)) {
            throw new BookingCancellationException("Non puoi annullare una prenotazione che non ti appartiene.");
        }
        if (slot.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingCancellationException("Questa prenotazione non può essere annullata (stato: " + slot.getStatus() + ").");
        }
        if (start.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BookingCancellationException("Non è possibile annullare una prenotazione a meno di 24 ore dall'appuntamento.");
        }

        slotService.cancelBooking(bookingId, userId);

        BookingStrategy refundStrategy = strategies.stream()
                .filter(s -> s.getSupportedRole() == professional.getRole())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nessuna strategy trovata per il ruolo: " + professional.getRole()));
        Optional<Subscription> subOpt = subscriptionService.findActiveByUserWithLock(client);
        if (subOpt.isEmpty()) {
            log.warn("Nessun abbonamento attivo per utente ID {}: rimborso non effettuato.", client.getId());
        } else {
            refundStrategy.refundCredits(subOpt.get());
            subscriptionService.save(subOpt.get());
        }

        try {
            emailService.sendBookingCancellationEmail(client.getEmail(), client.getFirstName(), professional.getFirstName(), start);
            emailService.sendBookingCancellationEmail(professional.getEmail(), professional.getFirstName(), client.getFirstName(), start);
        } catch (Exception e) {
            log.warn("Impossibile inviare email di cancellazione prenotazione: {}", e.getMessage());
        }
    }
}
