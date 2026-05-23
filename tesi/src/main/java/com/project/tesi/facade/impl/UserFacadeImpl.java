package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.BookingRequest;
import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.request.ReviewRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.booking.ProfessionalSoldOutException;
import com.project.tesi.exception.review.ReviewNotAllowedException;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.ReviewMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.*;
import com.project.tesi.repository.*;
import com.project.tesi.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserFacadeImpl implements UserFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFacadeImpl.class);
    private static final int MAX_CLIENTS_PER_PROFESSIONAL = 50;

    private final UserService userService;
    private final SlotService slotService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final ProfessionalStatsService professionalStatsService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final BookingMapper bookingMapper;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ReviewRepository reviewRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserFacadeImpl(UserService userService,
                          SlotService slotService,
                          ReviewService reviewService,
                          SubscriptionService subscriptionService,
                          ProfessionalStatsService professionalStatsService,
                          UserMapper userMapper,
                          SubscriptionMapper subscriptionMapper,
                          BookingMapper bookingMapper,
                          ReviewMapper reviewMapper,
                          UserRepository userRepository,
                          ChatRepository chatRepository,
                          ReviewRepository reviewRepository,
                          PlanRepository planRepository,
                          SubscriptionRepository subscriptionRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.userService = userService;
        this.slotService = slotService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.professionalStatsService = professionalStatsService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.bookingMapper = bookingMapper;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.reviewRepository = reviewRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Utente", "email", request.email());
        }

        User newUser = userMapper.toUser(request);
        newUser.setPassword(passwordEncoder.encode(request.password()));

        assignProfessional(newUser, request.selectedPtId(), Role.PERSONAL_TRAINER);
        assignProfessional(newUser, request.selectedNutritionistId(), Role.NUTRITIONIST);

        User savedUser = userService.save(newUser);

        if (request.selectedPlanId() != null && request.paymentFrequency() != null) {
            Plan selectedPlan = planRepository.findById(request.selectedPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Piano", request.selectedPlanId()));
            Subscription subscription = subscriptionMapper.toSubscription(request, savedUser, selectedPlan);
            subscriptionRepository.save(subscription);
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
            user.setPassword(passwordEncoder.encode(request.password().trim()));
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
            followingProfessionals.add(buildProfessionalSummary(user.getAssignedPT()));
        if (user.getAssignedNutritionist() != null)
            followingProfessionals.add(buildProfessionalSummary(user.getAssignedNutritionist()));

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
                    Double avg = reviewRepository.getAverageRating(pro.getId());
                    long activeClients = pro.getRole() == Role.PERSONAL_TRAINER
                            ? userService.countByAssignedPT(pro)
                            : userService.countByAssignedNutritionist(pro);
                    return ProfessionalSummaryDTO.builder()
                            .id(pro.getId())
                            .fullName(pro.getFullName())
                            .role(pro.getRole())
                            .averageRating(avg != null ? avg : 0.0)
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
            clients = userRepository.findByAssignedPT(professional);
        } else if (professional.getRole() == Role.NUTRITIONIST) {
            clients = userRepository.findByAssignedNutritionist(professional);
        } else {
            throw new IllegalArgumentException("L'utente non è un professionista");
        }

        return clients.stream()
                .map(this::toBasicInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getAdmin() {
        return userService.findByRole(Role.ADMIN).stream().findFirst()
                .map(this::toBasicInfo)
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
                return toBasicInfo(actor);
            }

            Optional<User> existing = findExistingOperatorConversation(actor.getId(), moderators);
            User selected = existing.orElseGet(() ->
                    moderators.stream()
                            .min(Comparator.comparingLong(m ->
                                    chatRepository.countOpenChatsByModerator(m.getId())))
                            .orElse(moderators.get(0))
            );
            return toBasicInfo(selected);
        }

        return toBasicInfo(moderators.get(0));
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        return slotService.createBooking(request, userId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        slotService.cancelBooking(bookingId, userId);
    }

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        User user = userService.getUserById(userId);
        User professional = userService.getUserById(request.professionalId());

        if (reviewService.existsByClientAndProfessional(user.getId(), professional.getId())) {
            throw new ResourceAlreadyExistsException("Hai già lasciato una recensione per questo professionista.");
        }
        if (!reviewService.canClientReview(user.getId(), professional.getId())) {
            throw new ReviewNotAllowedException(
                    "Puoi recensire solo professionisti con cui hai avuto un rapporto formale.");
        }

        Review review = Review.builder()
                .client(user)
                .professional(professional)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return reviewMapper.toResponse(reviewService.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return reviewMapper.toResponseList(reviewService.findByProfessional(professional));
    }

    @Override
    public boolean canClientReview(Long clientId, Long professionalId) {
        return reviewService.canClientReview(clientId, professionalId);
    }

    @Override
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewService.hasClientReviewed(clientId, professionalId);
    }

    @Override
    @Transactional
    public SubscriptionResponse activateSubscription(PlanRequest request, Long userId) {
        return subscriptionMapper.toResponse(subscriptionService.activateSubscription(request, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionStatus(Long userId) {
        return subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDTO> getAvailableSlots(Long professionalId) {
        return slotService.getAvailableSlots(professionalId);
    }

    @Override
    @Transactional
    public List<SlotDTO> createSlots(Long professionalId, List<SlotDTO> slots) {
        return slotService.createSlots(professionalId, slots);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long requesterId) {
        slotService.deleteSlot(slotId, requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalStatsResponse getProfessionalStats(Long professionalId) {
        return professionalStatsService.getProfessionalStats(professionalId);
    }

    private ProfessionalSummaryDTO buildProfessionalSummary(User pro) {
        return ProfessionalSummaryDTO.builder()
                .id(pro.getId())
                .fullName(pro.getFullName())
                .role(pro.getRole())
                .build();
    }

    // Assegna un professionista durante la registrazione, verificando il limite di clienti.
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

    private ClientBasicInfoResponse toBasicInfo(User user) {
        return ClientBasicInfoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    private Optional<User> findAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    private Optional<User> findExistingOperatorConversation(Long userId, List<User> operators) {
        List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
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
