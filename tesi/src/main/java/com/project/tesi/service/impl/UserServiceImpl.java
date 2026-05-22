package com.project.tesi.service.impl;

import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.BookingResponse;
import com.project.tesi.dto.response.ClientBasicInfoResponse;
import com.project.tesi.dto.response.ClientDashboardResponse;
import com.project.tesi.dto.response.ProfessionalSummaryDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.booking.ProfessionalSoldOutException;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.model.Chat;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.EmailService;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Gestisce la logica di business legata al profilo utente e alle assegnazioni.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // Limite rigido: un professionista non può seguire più di 50 clienti attivi contemporaneamente.
    // Serve a garantire che la qualità del servizio (risposte in chat, schede) non degradi.
    private static final int MAX_CLIENTS_PER_PROFESSIONAL = 50;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ChatRepository chatRepository;
    private final ReviewRepository reviewRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final BookingMapper bookingMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           BookingRepository bookingRepository,
                           ChatRepository chatRepository,
                           ReviewRepository reviewRepository,
                           PlanRepository planRepository,
                           SubscriptionRepository subscriptionRepository,
                           UserMapper userMapper,
                           SubscriptionMapper subscriptionMapper,
                           BookingMapper bookingMapper,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.chatRepository = chatRepository;
        this.reviewRepository = reviewRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.bookingMapper = bookingMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        // Aggiornamento parziale del profilo: controlliamo esplicitamente null e
        // stringhe vuote per evitare di piallare i campi esistenti con dati sporchi.
        if (request.firstName() != null && !request.firstName().trim().isEmpty()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().trim().isEmpty()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.profilePicture() != null && !request.profilePicture().trim().isEmpty()) {
            user.setProfilePicture(request.profilePicture().trim());
        }
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password().trim()));
            try {
                emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
            } catch (Exception e) {
                log.warn("Impossibile inviare email di cambio password a {}: {}", user.getEmail(), e.getMessage());
            }
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Utente", "email", request.email());
        }

        User newUser = userMapper.toUser(request);
        newUser.setPassword(passwordEncoder.encode(request.password()));

        assignProfessional(newUser, request.selectedPtId(), Role.PERSONAL_TRAINER);
        assignProfessional(newUser, request.selectedNutritionistId(), Role.NUTRITIONIST);

        User savedUser = userRepository.save(newUser);

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
    @Transactional(readOnly = true)
    public List<ProfessionalSummaryDTO> findAvailableProfessionals(Role role) {
        return userRepository.findByRole(role).stream()
                .map(pro -> {
                    Double avg = reviewRepository.getAverageRating(pro.getId());
                    long activeClients = pro.getRole() == Role.PERSONAL_TRAINER
                            ? userRepository.countByAssignedPT(pro)
                            : userRepository.countByAssignedNutritionist(pro);

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
    public ClientDashboardResponse getClientDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

        if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            List<BookingResponse> proBookingResponses = bookingRepository.findByProfessional(user).stream()
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
        if (user.getAssignedPT() != null) {
            followingProfessionals.add(buildProfessionalSummary(user.getAssignedPT()));
        }
        if (user.getAssignedNutritionist() != null) {
            followingProfessionals.add(buildProfessionalSummary(user.getAssignedNutritionist()));
        }

        SubscriptionResponse subResponse = null;
        Optional<Subscription> subOpt = subscriptionRepository.findByUserAndActiveTrue(user);
        if (subOpt.isPresent()) {
            Subscription sub = subOpt.get();
            subResponse = SubscriptionResponse.builder()
                    .id(sub.getId())
                    .planName(sub.getPlan().getName())
                    .startDate(sub.getStartDate())
                    .endDate(sub.getEndDate())
                    .active(sub.isActive())
                    .currentCreditsPT(sub.getCurrentCreditsPT())
                    .currentCreditsNutri(sub.getCurrentCreditsNutri())
                    .build();
        }

        List<BookingResponse> upcomingBookings = bookingRepository.findFutureByUser(user, LocalDateTime.now()).stream()
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
    public List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId) {
        User professional = userRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", professionalId));

        List<User> clients;
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            clients = userRepository.findByAssignedPT(professional);
        } else if (professional.getRole() == Role.NUTRITIONIST) {
            clients = userRepository.findByAssignedNutritionist(professional);
        } else {
            throw new IllegalArgumentException("L'utente non e un professionista");
        }

        return clients.stream()
                .map(client -> ClientBasicInfoResponse.builder()
                        .id(client.getId())
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .email(client.getEmail())
                        .profilePictureUrl(client.getProfilePicture())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getSupportOperator() {
        List<User> moderators = userRepository.findByRole(Role.MODERATOR);

        if (!moderators.isEmpty()) {
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

        throw new ResourceNotFoundException("Nessun moderatore trovato nel sistema.");
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getAdmin() {
        return userRepository.findByRole(Role.ADMIN).stream().findFirst()
                .map(this::toBasicInfo)
                .orElseThrow(() -> new ResourceNotFoundException("Amministratore non trovato nel sistema."));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));
    }

    private ProfessionalSummaryDTO buildProfessionalSummary(User pro) {
        return ProfessionalSummaryDTO.builder()
                .id(pro.getId())
                .fullName(pro.getFullName())
                .role(pro.getRole())
                .build();
    }

    // Metodo per assegnare un professionista a un utente durante la registrazione.
    // L'assegnazione è guidata dall'utente (passa lui l'ID del professionista scelto).
    // Blocchiamo l'operazione se il professionista ha già raggiunto il limite di MAX_CLIENTS_PER_PROFESSIONAL.
    private void assignProfessional(User user, Long proId, Role expectedRole) {
        if (proId == null) {
            throw new IllegalArgumentException("Devi selezionare un " + expectedRole);
        }

        User professional = userRepository.findById(proId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionista", proId));

        if (professional.getRole() != expectedRole) {
            throw new IllegalArgumentException("L'ID fornito non corrisponde a un " + expectedRole + ".");
        }

        long activeClients = expectedRole == Role.PERSONAL_TRAINER
                ? userRepository.countByAssignedPT(professional)
                : userRepository.countByAssignedNutritionist(professional);
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

