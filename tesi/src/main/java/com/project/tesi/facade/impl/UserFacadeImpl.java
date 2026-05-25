package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.booking.ProfessionalSoldOutException;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.*;
import com.project.tesi.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.project.tesi.enums.DocumentType;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserFacadeImpl implements UserFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFacadeImpl.class);
    private static final int MAX_CLIENTS_PER_PROFESSIONAL = 50;

    private final UserService userService;
    private final PlanService planService;
    private final SlotService slotService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionFacade subscriptionFacade;
    private final ChatService chatService;
    private final ProfessionalStatsService professionalStatsService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    public UserFacadeImpl(UserService userService,
                          PlanService planService,
                          SlotService slotService,
                          ReviewService reviewService,
                          SubscriptionService subscriptionService,
                          SubscriptionFacade subscriptionFacade,
                          ChatService chatService,
                          ProfessionalStatsService professionalStatsService,
                          UserMapper userMapper,
                          SubscriptionMapper subscriptionMapper,
                          BookingMapper bookingMapper,
                          EmailService emailService) {
        this.userService = userService;
        this.planService = planService;
        this.slotService = slotService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.subscriptionFacade = subscriptionFacade;
        this.chatService = chatService;
        this.professionalStatsService = professionalStatsService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.bookingMapper = bookingMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Utente", "email", request.email());
        }

        User newUser = userMapper.toUser(request);
        newUser.setPassword(userService.encodePassword(request.password()));

        assignProfessional(newUser, request.selectedPtId(), Role.PERSONAL_TRAINER);
        assignProfessional(newUser, request.selectedNutritionistId(), Role.NUTRITIONIST);

        User savedUser = userService.save(newUser);

        if (request.selectedPlanId() != null && request.paymentFrequency() != null) {
            subscriptionFacade.activateSubscription(
                    new PlanRequest(request.selectedPlanId(), request.paymentFrequency()),
                    savedUser.getId());
        }

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di benvenuto a {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userService.getUserById(userId);

        if (request.firstName() != null && !request.firstName().trim().isEmpty())
            user.setFirstName(request.firstName().trim());
        if (request.lastName() != null && !request.lastName().trim().isEmpty())
            user.setLastName(request.lastName().trim());
        if (request.profilePicture() != null && !request.profilePicture().trim().isEmpty())
            user.setProfilePicture(request.profilePicture().trim());
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(userService.encodePassword(request.password().trim()));
            try {
                emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
            } catch (Exception e) {
                log.warn("Impossibile inviare email cambio password a {}: {}", user.getEmail(), e.getMessage());
            }
        }

        userService.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDashboardResponse getClientDashboard(Long userId) {
        User user = userService.getUserById(userId);

        if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            List<BookingResponse> proBookingResponses = slotService.findBookingsByProfessional(user)
                    .stream()
                    .map(bookingMapper::toResponse)
                    .collect(Collectors.toList());

            return ClientDashboardResponse.builder()
                    .profile(userMapper.toUserResponse(user))
                    .followingProfessionals(new ArrayList<>())
                    .subscription(null)
                    .upcomingBookings(proBookingResponses)
                    .build();
        }

        List<ProfessionalSummaryDTO> followingProfessionals = new ArrayList<>();
        if (user.getAssignedPT() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedPT()));
        if (user.getAssignedNutritionist() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedNutritionist()));

        SubscriptionResponse subResponse = null;
        try {
            subResponse = subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(userId));
        } catch (Exception ignored) {}

        List<BookingResponse> upcomingBookings = slotService.findFutureByUser(user, LocalDateTime.now())
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());

        return ClientDashboardResponse.builder()
                .profile(userMapper.toUserResponse(user))
                .followingProfessionals(followingProfessionals)
                .subscription(subResponse)
                .upcomingBookings(upcomingBookings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role) {
        return userService.findByRole(role).stream()
                .map(pro -> {
                    double avg = reviewService.getAverageRating(pro.getId());
                    long activeClients = pro.getRole() == Role.PERSONAL_TRAINER
                            ? userService.countByAssignedPT(pro)
                            : userService.countByAssignedNutritionist(pro);
                    return ProfessionalSummaryDTO.builder()
                            .id(pro.getId())
                            .fullName(pro.getFullName())
                            .role(pro.getRole())
                            .averageRating(avg)
                            .currentActiveClients((int) activeClients)
                            .isSoldOut(activeClients >= MAX_CLIENTS_PER_PROFESSIONAL)
                            .build();
                })
                .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId) {
        User professional = userService.getUserById(professionalId);

        List<User> clients;
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            clients = userService.findByAssignedPT(professional);
        } else if (professional.getRole() == Role.NUTRITIONIST) {
            clients = userService.findByAssignedNutritionist(professional);
        } else {
            throw new IllegalArgumentException("L'utente non è un professionista");
        }

        return clients.stream()
                .map(userMapper::toBasicInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getAdmin() {
        return userService.findByRole(Role.ADMIN).stream().findFirst()
                .map(userMapper::toBasicInfoResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Amministratore non trovato nel sistema."));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getModerator() {
        List<User> moderators = userService.findByRole(Role.MODERATOR);

        if (moderators.isEmpty()) {
            throw new ResourceNotFoundException("Nessun moderatore trovato nel sistema.");
        }

        Optional<User> currentUser = findAuthenticatedUser();
        if (currentUser.isPresent()) {
            User actor = currentUser.get();

            if (actor.getRole() == Role.MODERATOR || actor.getRole() == Role.ADMIN) {
                return userMapper.toBasicInfoResponse(actor);
            }

            Optional<User> existing = findExistingOperatorConversation(actor.getId(), moderators);
            User selected = existing.orElseGet(() ->
                    moderators.stream()
                            .min(Comparator.comparingLong(m ->
                                    chatService.countOpenChatsByModerator(m.getId())))
                            .orElse(moderators.get(0))
            );
            return userMapper.toBasicInfoResponse(selected);
        }

        return userMapper.toBasicInfoResponse(moderators.get(0));
    }

    @Override
    @Transactional
    public SubscriptionResponse activateSubscription(PlanRequest request, Long userId) {
        return subscriptionMapper.toResponse(subscriptionFacade.activateSubscription(request, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionStatus(Long userId) {
        return subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalStatsResponse getProfessionalStats(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        if (professional.getRole() != Role.PERSONAL_TRAINER && professional.getRole() != Role.NUTRITIONIST) {
            throw new IllegalArgumentException("L'utente con ID " + professionalId + " non è un professionista.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        List<Slot> todaySlots = professionalStatsService.getTodaySlots(professional, dayStart, dayEnd);

        List<ProfessionalStatsResponse.TodayBookingItem> todayList = todaySlots.stream().map(s ->
                new ProfessionalStatsResponse.TodayBookingItem(
                        s.getId(),
                        s.getBookedBy() != null ? s.getBookedBy().getFullName() : "",
                        s.getBookedBy() != null ? s.getBookedBy().getId() : null,
                        s.getStartTime().toLocalTime().toString().substring(0, 5),
                        s.getEndTime().toLocalTime().toString().substring(0, 5),
                        s.getStatus() != null ? s.getStatus().name() : "",
                        s.getMeetingLink()
                )).collect(Collectors.toList());

        DocumentType relevantDocType = professional.getRole() == Role.PERSONAL_TRAINER
                ? DocumentType.WORKOUT_PLAN : DocumentType.DIET_PLAN;
        List<User> clients = professionalStatsService.getAssignedClients(professional);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ProfessionalStatsResponse.ClientAttentionItem> clientsNeedingAttention = new ArrayList<>();
        for (User client : clients) {
            Document latestDoc = professionalStatsService.getLatestDocumentByOwnerAndType(client, relevantDocType);
            boolean needsAttention = (latestDoc == null || latestDoc.getUploadDate().isBefore(sevenDaysAgo));
            if (needsAttention) {
                clientsNeedingAttention.add(new ProfessionalStatsResponse.ClientAttentionItem(
                        client.getId(),
                        client.getFirstName(),
                        client.getLastName(),
                        latestDoc != null ? latestDoc.getUploadDate().toString() : null,
                        latestDoc != null
                                ? Duration.between(latestDoc.getUploadDate(), LocalDateTime.now()).toDays()
                                : -1));
            }
        }

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int docsUploadedThisWeek = professionalStatsService.countDocumentsUploadedSince(
                professional, startOfWeek.atStartOfDay());

        return new ProfessionalStatsResponse(
                todayList,
                todayList.size(),
                clientsNeedingAttention,
                clientsNeedingAttention.size(),
                docsUploadedThisWeek,
                clients.size());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assignProfessional(User user, Long proId, Role expectedRole) {
        if (proId == null) {
            throw new IllegalArgumentException("Devi selezionare un " + expectedRole);
        }
        User professional = userService.getUserById(proId);

        if (professional.getRole() != expectedRole) {
            throw new IllegalArgumentException("L'ID fornito non corrisponde a un " + expectedRole + ".");
        }

        long activeClients = expectedRole == Role.PERSONAL_TRAINER
                ? userService.countByAssignedPT(professional)
                : userService.countByAssignedNutritionist(professional);
        if (activeClients >= MAX_CLIENTS_PER_PROFESSIONAL) {
            throw new ProfessionalSoldOutException(professional.getFirstName());
        }

        if (expectedRole == Role.PERSONAL_TRAINER) {
            user.setAssignedPT(professional);
        } else {
            user.setAssignedNutritionist(professional);
        }
    }

    private Optional<User> findAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        try {
            return Optional.of(userService.getUserByEmail(authentication.getName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<User> findExistingOperatorConversation(Long userId, List<User> operators) {
        List<Chat> chats = chatService.getUserConversations(userId);
        if (chats == null || chats.isEmpty()) {
            return Optional.empty();
        }
        List<User> partners = chats.stream()
                .map(c -> c.getUser1().getId().equals(userId) ? c.getUser2() : c.getUser1())
                .collect(Collectors.toList());
        return partners.stream()
                .filter(p -> operators.stream().anyMatch(o -> o.getId().equals(p.getId())))
                .findFirst();
    }
}
