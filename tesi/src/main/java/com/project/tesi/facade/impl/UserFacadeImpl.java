package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.request.ProfileUpdateRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.facade.UserFacade;
import com.project.tesi.util.BusinessConstants;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.*;
import com.project.tesi.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserFacadeImpl implements UserFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFacadeImpl.class);

    private final UserService userService;
    private final PlanService planService;
    private final SlotService slotService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final DocumentService documentService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;
    private final SubscriptionFacade subscriptionFacade;

    public UserFacadeImpl(UserService userService,
                          PlanService planService,
                          SlotService slotService,
                          ReviewService reviewService,
                          SubscriptionService subscriptionService,
                          DocumentService documentService,
                          UserMapper userMapper,
                          SubscriptionMapper subscriptionMapper,
                          BookingMapper bookingMapper,
                          EmailService emailService,
                          SubscriptionFacade subscriptionFacade) {
        this.userService = userService;
        this.planService = planService;
        this.slotService = slotService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.documentService = documentService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.bookingMapper = bookingMapper;
        this.emailService = emailService;
        this.subscriptionFacade = subscriptionFacade;
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
        if (user.getRole() != Role.CLIENT) {
            throw new UnauthorizedAccessException("La dashboard cliente è accessibile solo ai clienti.");
        }

        List<ProfessionalSummaryDTO> followingProfessionals = new ArrayList<>();
        if (user.getAssignedPT() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedPT()));
        if (user.getAssignedNutritionist() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedNutritionist()));

        SubscriptionResponse subResponse = null;
        try {
            subResponse = subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(user));
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
                            .isSoldOut(activeClients >= BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL)
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
    @Transactional
    public SubscriptionResponse activateSubscription(PlanRequest request, Long userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != Role.CLIENT) {
            throw new UnauthorizedAccessException("Solo i clienti possono attivare un abbonamento.");
        }
        if (subscriptionService.findActiveByUser(user).isPresent()) {
            throw new ResourceAlreadyExistsException("L'utente ha già un abbonamento attivo. Contattare l'amministrazione per cambiare piano.");
        }
        Plan plan = planService.getPlanById(request.planId());
        return subscriptionMapper.toResponse(subscriptionFacade.activateSubscription(user, plan, request.paymentFrequency()));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionStatus(Long userId) {
        User user = userService.getUserById(userId);
        return subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(user));
    }

}
