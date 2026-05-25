package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.BookingCancellationException;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.booking.SubscriptionExpiredException;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.exception.common.BusinessLogicException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.BookingFacade;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.*;
import com.project.tesi.service.strategy.BookingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class BookingFacadeImpl implements BookingFacade {

    private static final Logger log = LoggerFactory.getLogger(BookingFacadeImpl.class);

    private static class LockReference {
        final ReentrantLock lock = new ReentrantLock();
        int count = 0;
    }

    /**
     * Mappa per gestire i lock a grana fine sugli slot.
     * ConcurrentHashMap con ReentrantLock per slot — shared resource + lock richiesto dal corso.
     */
    private final Map<Long, LockReference> slotLocks = new ConcurrentHashMap<>();

    private final UserService userService;
    private final SlotService slotService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionFacade subscriptionFacade;
    private final ActivityFeedService activityFeedService;
    private final VideoConferenceService videoConferenceService;
    private final EmailService emailService;
    private final List<BookingStrategy> strategies;
    private final BookingMapper bookingMapper;
    private final SlotMapper slotMapper;

    public BookingFacadeImpl(UserService userService,
                             SlotService slotService,
                             SubscriptionService subscriptionService,
                             SubscriptionFacade subscriptionFacade,
                             ActivityFeedService activityFeedService,
                             VideoConferenceService videoConferenceService,
                             EmailService emailService,
                             List<BookingStrategy> strategies,
                             BookingMapper bookingMapper,
                             SlotMapper slotMapper) {
        this.userService = userService;
        this.slotService = slotService;
        this.subscriptionService = subscriptionService;
        this.subscriptionFacade = subscriptionFacade;
        this.activityFeedService = activityFeedService;
        this.videoConferenceService = videoConferenceService;
        this.emailService = emailService;
        this.strategies = strategies;
        this.bookingMapper = bookingMapper;
        this.slotMapper = slotMapper;
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

            Subscription sub = subscriptionService.getSubscriptionStatus(userId);
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

            subscriptionFacade.deductCredits(saved);
            activityFeedService.logBookingCreated(saved);

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

        subscriptionFacade.refundCreditsIfActive(client, professional.getRole());

        try {
            emailService.sendBookingCancellationEmail(client.getEmail(), client.getFirstName(), professional.getFirstName(), start);
            emailService.sendBookingCancellationEmail(professional.getEmail(), professional.getFirstName(), client.getFirstName(), start);
        } catch (Exception e) {
            log.warn("Impossibile inviare email di cancellazione prenotazione: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        return slotMapper.toDtoList(slotService.getAvailableSlots(professionalId));
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
        slotService.deleteSlot(slotId, requesterId);
    }
}
