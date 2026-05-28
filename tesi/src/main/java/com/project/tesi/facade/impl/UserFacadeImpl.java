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

/**
 * Implementazione di {@link UserFacade}.
 * Aggrega dati da {@code UserService}, {@code SubscriptionService}, {@code SlotService}
 * e altri servizi per costruire la dashboard e gestire il profilo utente.
 */
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

    /**
     * Aggiorna nome, cognome e immagine profilo dell'utente se i relativi campi
     * della richiesta sono non nulli e non vuoti. Se viene fornita una nuova password,
     * la codifica e invia un'email di notifica avvenuta modifica (errori SMTP non bloccanti).
     *
     * @param userId  identificatore dell'utente da aggiornare
     * @param request dati di aggiornamento del profilo
     */
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

    /**
     * Aggrega in {@link ClientDashboardResponse} il profilo utente, l'abbonamento attivo
     * (se presente), i professionisti assegnati (PT e/o nutrizionista) e la lista degli
     * appuntamenti futuri. Accessibile solo agli utenti con ruolo {@code CLIENT}.
     *
     * @param userId identificatore del cliente
     * @return {@link ClientDashboardResponse} con tutti i dati aggregati della dashboard
     * @throws UnauthorizedAccessException se l'utente non ha ruolo CLIENT
     */
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

    /**
     * Risolve piano e frequenza di pagamento dalla {@link PlanRequest}, verifica che
     * l'utente sia un CLIENT senza abbonamento attivo, poi delega l'attivazione
     * a {@code SubscriptionFacade} e restituisce la risposta mappata.
     *
     * @param request dati del piano (planId, paymentFrequency)
     * @param userId  identificatore del cliente che attiva l'abbonamento
     * @return {@link SubscriptionResponse} con i dati dell'abbonamento attivato
     * @throws UnauthorizedAccessException    se l'utente non è un CLIENT
     * @throws ResourceAlreadyExistsException se il cliente ha già un abbonamento attivo
     */
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
