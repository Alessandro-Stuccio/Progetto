package com.project.tesi.service.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.SlotDTO;
import com.project.tesi.enums.BookingStatus;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.BookingCancellationException;
import com.project.tesi.exception.booking.NoActiveSubscriptionException;
import com.project.tesi.exception.booking.SlotAlreadyBookedException;
import com.project.tesi.exception.booking.SubscriptionExpiredException;
import com.project.tesi.exception.common.BusinessLogicException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SlotMapper;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.VideoConferenceService;
import com.project.tesi.service.strategy.BookingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final SlotMapper slotMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingMapper bookingMapper;
    private final List<BookingStrategy> strategies;
    private final VideoConferenceService videoConferenceService;
    private final ActivityFeedService activityFeedService;
    private final EmailService emailService;

    private static class LockReference {
        final ReentrantLock lock = new ReentrantLock();
        int count = 0;
    }

    /**
     * Mappa per gestire i lock a grana fine sugli slot.
     * ConcurrentHashMap con ReentrantLock per slot — shared resource + lock richiesto dal corso.
     */
    private final Map<Long, LockReference> slotLocks = new ConcurrentHashMap<>();

    public SlotServiceImpl(SlotRepository slotRepository,
                           UserRepository userRepository,
                           WeeklyScheduleRepository weeklyScheduleRepository,
                           SlotMapper slotMapper,
                           SubscriptionRepository subscriptionRepository,
                           BookingMapper bookingMapper,
                           List<BookingStrategy> strategies,
                           VideoConferenceService videoConferenceService,
                           ActivityFeedService activityFeedService,
                           EmailService emailService) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.slotMapper = slotMapper;
        this.subscriptionRepository = subscriptionRepository;
        this.bookingMapper = bookingMapper;
        this.strategies = strategies;
        this.videoConferenceService = videoConferenceService;
        this.activityFeedService = activityFeedService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slotsDTO) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono creare slot");
        }

        List<Slot> slotsToSave = new ArrayList<>();

        for (SlotDTO dto : slotsDTO) {
            Slot slot = Slot.builder()
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .professional(professional)
                    .bookedBy(null)
                    .build();
            slotsToSave.add(slot);
        }

        List<Slot> savedSlots = slotRepository.saveAll(slotsToSave);

        return savedSlots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void generateSlotsFromSchedule(Long professionalId, LocalDate startDate, LocalDate endDate) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new UnauthorizedAccessException("Solo i professionisti possono generare slot");
        }

        List<WeeklySchedule> schedules = weeklyScheduleRepository.findByProfessional(professional);

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

                    if (!slotRepository.existsByProfessionalAndStartTime(professional, startSlot)) {
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
            slotRepository.saveAll(newSlots);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        return slotRepository.findByProfessionalAndBookedByIsNull(professional).stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Slot", slotId);
        }
        slotRepository.deleteById(slotId);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long requesterId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));
        if (!slot.getProfessional().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("Non sei autorizzato a eliminare questo slot");
        }
        slotRepository.deleteById(slotId);
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

            Slot slot = slotRepository.findByIdWithLock(slotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

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
            slot.setStatus(BookingStatus.CONFIRMED);
            String meetLink = videoConferenceService.generateMeetingLink(user, professional, slot);
            slot.setMeetingLink(meetLink);
            slot.setBookedAt(LocalDateTime.now());
            Slot saved = slotRepository.save(slot);

            activityFeedService.logBookingCreated(saved);

            emailService.sendBookingConfirmationEmail(user.getEmail(), user.getFirstName(), professional.getFirstName(), slot.getStartTime(), meetLink);
            emailService.sendBookingConfirmationEmail(professional.getEmail(), professional.getFirstName(), user.getFirstName(), slot.getStartTime(), meetLink);

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
    public void cancelBooking(Long slotId, Long userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione", slotId));

        if (slot.getBookedBy() == null || !slot.getBookedBy().getId().equals(userId)) {
            throw new BookingCancellationException("Non puoi annullare una prenotazione che non ti appartiene.");
        }

        if (slot.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingCancellationException("Questa prenotazione non può essere annullata (stato: " + slot.getStatus() + ").");
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BookingCancellationException("Non è possibile annullare una prenotazione a meno di 24 ore dall'appuntamento.");
        }

        User professional = slot.getProfessional();
        User client = slot.getBookedBy();

        BookingStrategy strategy = strategies.stream()
                .filter(s -> s.getSupportedRole() == professional.getRole())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Strategia non trovata"));

        Subscription sub = subscriptionRepository.findByUserAndActiveTrue(client).orElse(null);
        if (sub != null) {
            strategy.refundCredits(sub);
            subscriptionRepository.save(sub);
        } else {
            log.warn("Nessun abbonamento attivo per utente ID {}: rimborso non effettuato (slot ID {}).", client.getId(), slotId);
        }

        String clientEmail = client.getEmail();
        String clientName  = client.getFirstName();
        String profEmail   = professional.getEmail();
        String profName    = professional.getFirstName();
        LocalDateTime start = slot.getStartTime();

        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CANCELED);
        slot.setMeetingLink(null);
        slot.setBookedAt(null);
        slotRepository.save(slot);

        emailService.sendBookingCancellationEmail(clientEmail, clientName, profName, start);
        emailService.sendBookingCancellationEmail(profEmail, profName, clientName, start);
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
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Slot> findFutureByUser(User user, LocalDateTime from) {
        return slotRepository.findFutureByBookedBy(user, from);
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void generateWeeklySlotsForAllProfessionals() {
        List<User> professionals = userRepository.findByRoleIn(List.of(Role.PERSONAL_TRAINER, Role.NUTRITIONIST));
        LocalDate start = LocalDate.now().plusDays(7);
        LocalDate end = start.plusDays(6);
        for (User pro : professionals) {
            generateSlotsFromSchedule(pro.getId(), start, end);
        }
    }
}
