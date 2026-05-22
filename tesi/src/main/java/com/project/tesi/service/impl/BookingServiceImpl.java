package com.project.tesi.service.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.BookingCancellationException;
import com.project.tesi.exception.booking.NoActiveSubscriptionException;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.booking.SubscriptionExpiredException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.service.BookingService;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.VideoConferenceService;
import com.project.tesi.service.strategy.BookingStrategy;
import com.project.tesi.builder.BookingDirector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingMapper bookingMapper;
    private final List<BookingStrategy> strategies;
    private final VideoConferenceService videoConferenceService;
    private final ActivityFeedService activityFeedService;
    private final EmailService emailService;
    private final BookingDirector bookingDirector;

    private static class LockReference {
        final ReentrantLock lock = new ReentrantLock();
        int count = 0;
    }

    /**
     * Mappa per gestire i lock a grana fine sugli slot.
     * ConcurrentHashMap con ReentrantLock per slot — shared resource + lock richiesto dal corso.
     */
    private final Map<Long, LockReference> slotLocks = new ConcurrentHashMap<>();

    public BookingServiceImpl(BookingRepository bookingRepository, SlotRepository slotRepository,
                              UserRepository userRepository, SubscriptionRepository subscriptionRepository,
                              BookingMapper bookingMapper, List<BookingStrategy> strategies,
                              VideoConferenceService videoConferenceService,
                              ActivityFeedService activityFeedService, EmailService emailService,
                              BookingDirector bookingDirector) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.bookingMapper = bookingMapper;
        this.strategies = strategies;
        this.videoConferenceService = videoConferenceService;
        this.activityFeedService = activityFeedService;
        this.emailService = emailService;
        this.bookingDirector = bookingDirector;
    }

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
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

            Slot slot = slotRepository.findByIdWithLock(request.slotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Slot", request.slotId()));

            if (slot.getBookedBy() != null) {
                throw new SlotAlreadyBookedException("Slot non più disponibile");
            }

            if (bookingRepository.existsBySlotAndSlotStatus(slot, BookingStatus.CONFIRMED)) {
                throw new SlotAlreadyBookedException("Esiste già una prenotazione confermata per questo slot.");
            }

            bookingRepository.deleteBySlotAndStatus(slot, BookingStatus.CANCELED);

            User professional = slot.getProfessional();

            BookingStrategy strategy = strategies.stream()
                    .filter(s -> s.getSupportedRole() == professional.getRole())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Il professionista non è né PT né Nutrizionista"));

            strategy.verifyAssignment(user, professional);

            Subscription sub = subscriptionRepository.findByUserAndActiveTrue(user)
                    .orElseThrow(NoActiveSubscriptionException::new);

            LocalDate today = LocalDate.now();
            if (today.isAfter(sub.getEndDate())) {
                throw new SubscriptionExpiredException(
                        "Impossibile prenotare: il tuo abbonamento è scaduto in data " + sub.getEndDate() + "."
                );
            } else if (slot.getStartTime().toLocalDate().isAfter(sub.getEndDate())) {
                throw new SubscriptionExpiredException(
                        "Operazione rifiutata: l'abbonamento scadrà il " + sub.getEndDate() +
                        ", prima della data prevista per questo slot (" + slot.getStartTime().toLocalDate() + ")."
                );
            }

            strategy.consumeCredits(sub);
            subscriptionRepository.save(sub);

            slot.setBookedBy(user);
            String meetLink = videoConferenceService.generateMeetingLink(user, professional, slot);
            slot.setMeetingLink(meetLink);
            slotRepository.save(slot);

            Booking booking = bookingDirector.buildConfirmedBooking(user, slot);

            Booking saved = bookingRepository.save(booking);

            activityFeedService.logBookingCreated(saved);

            String clientEmail  = saved.getUser().getEmail();
            String clientName   = saved.getUser().getFirstName();
            String profEmail    = professional.getEmail();
            String profName     = professional.getFirstName();
            LocalDateTime start = slot.getStartTime();

            emailService.sendBookingConfirmationEmail(clientEmail, clientName, profName, start, meetLink);
            emailService.sendBookingConfirmationEmail(profEmail, profName, clientName, start, meetLink);

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

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione", bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingCancellationException("Non puoi annullare una prenotazione che non ti appartiene.");
        }

        Slot slot = booking.getSlot();

        if (slot.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingCancellationException("Questa prenotazione non può essere annullata (stato: " + slot.getStatus() + ").");
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BookingCancellationException("Non è possibile annullare una prenotazione a meno di 24 ore dall'appuntamento.");
        }

        User professional = slot.getProfessional();

        BookingStrategy strategy = strategies.stream()
                .filter(s -> s.getSupportedRole() == professional.getRole())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Strategia non trovata"));

        Subscription sub = subscriptionRepository.findByUserAndActiveTrue(booking.getUser())
                .orElse(null);
        if (sub != null) {
            strategy.refundCredits(sub);
            subscriptionRepository.save(sub);
        } else {
            log.warn("Nessun abbonamento attivo trovato per l'utente ID {}: impossibile elaborare il rimborso (prenotazione ID {}).", booking.getUser().getId(), bookingId);
        }

        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CANCELED);
        slotRepository.save(slot);

        bookingRepository.save(booking);

        String clientEmail  = booking.getUser().getEmail();
        String clientName   = booking.getUser().getFirstName();
        String profEmail    = professional.getEmail();
        String profName     = professional.getFirstName();
        LocalDateTime start = slot.getStartTime();

        emailService.sendBookingCancellationEmail(clientEmail, clientName, profName, start);
        emailService.sendBookingCancellationEmail(profEmail, profName, clientName, start);
    }

    @Override
    public List<Booking> findRecentByUser(User user, LocalDateTime since) {
        return bookingRepository.findRecentByUser(user, since);
    }

    @Override
    public List<Booking> findRecentByProfessional(User professional, LocalDateTime since) {
        return bookingRepository.findRecentByProfessional(professional, since);
    }
}
