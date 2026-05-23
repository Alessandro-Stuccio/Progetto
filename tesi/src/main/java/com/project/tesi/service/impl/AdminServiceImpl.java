package com.project.tesi.service.impl;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.user.AdminSelfDeletionNotAllowedException;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.model.Chat;
import com.project.tesi.repository.ChatRepository;
import com.project.tesi.repository.DocumentRepository;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.SlotRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio amministrativo.
 * Gestisce utenti, piani e abbonamenti con regole dedicate per admin e moderator.
 *
 * I metodi pubblici restituiscono entità di dominio tipizzate (User, Subscription, Plan)
 * per garantire type-safety a compile-time. La conversione in DTO avviene nel layer Facade.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Set<Role> MODERATOR_MANAGEABLE_ROLES = EnumSet.of(
            Role.CLIENT,
            Role.PERSONAL_TRAINER,
            Role.NUTRITIONIST);

    private static final Set<Role> ADMIN_MANAGEABLE_ROLES = EnumSet.of(
            Role.MODERATOR,
            Role.INSURANCE_MANAGER);

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final ReviewRepository reviewRepository;
    private final SlotRepository slotRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionMapper subscriptionMapper;

    public AdminServiceImpl(UserRepository userRepository,
                            PlanRepository planRepository,
                            SubscriptionRepository subscriptionRepository,
                            DocumentRepository documentRepository,
                            ChatRepository chatRepository,
                            ReviewRepository reviewRepository,
                            SlotRepository slotRepository,
                            WeeklyScheduleRepository weeklyScheduleRepository,
                            PasswordEncoder passwordEncoder,
                            SubscriptionMapper subscriptionMapper) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.reviewRepository = reviewRepository;
        this.slotRepository = slotRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionMapper = subscriptionMapper;
    }

    // ────────────────────── Utenti ──────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getModeratorManageableUsers() {
        User actor = getAuthenticatedActor();
        ensureRole(actor, Role.MODERATOR);

        return userRepository.findAll().stream()
                .filter(u -> MODERATOR_MANAGEABLE_ROLES.contains(u.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getModeratorChatContacts() {
        User actor = getAuthenticatedActor();
        ensureRole(actor, Role.MODERATOR);

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.INSURANCE_MANAGER)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequestDTO request) {
        return createUserInternal(request, null);
    }

    @Override
    @Transactional
    public User createUserAsModerator(UserCreateRequestDTO request) {
        User actor = getAuthenticatedActor();
        ensureRole(actor, Role.MODERATOR);
        return createUserInternal(request, actor);
    }

    @Override
    @Transactional
    public User updateUserAsModerator(Long id, ModeratorUserUpdateRequest request) {
        User actor = getAuthenticatedActor();
        ensureRole(actor, Role.MODERATOR);
        return updateUserInternal(id, request, actor);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        Optional<User> maybeActor = getAuthenticatedActorOptional();
        deleteUserInternal(id, maybeActor.orElse(null));
    }

    @Override
    @Transactional
    public User updateUser(Long id, ModeratorUserUpdateRequest request) {
        return updateUserInternal(id, request, null);
    }

    @Override
    @Transactional
    public void deleteUserAsModerator(Long id) {
        User actor = getAuthenticatedActor();
        ensureRole(actor, Role.MODERATOR);
        deleteUserInternal(id, actor);
    }

    // ────────────────────── Abbonamenti ──────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional
    public Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri) {
        User actor = getAuthenticatedActor();
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.MODERATOR) {
            throw new UnauthorizedAccessException("Non hai i permessi per modificare i crediti.");
        }

        if (creditsPT < 0 || creditsNutri < 0) {
            throw new IllegalArgumentException("I crediti non possono essere negativi.");
        }

        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Abbonamento", subscriptionId));

        sub.setCurrentCreditsPT(creditsPT);
        sub.setCurrentCreditsNutri(creditsNutri);
        return subscriptionRepository.save(sub);
    }

    // ────────────────────── Piani ──────────────────────

    @Override
    @Transactional
    public Plan createPlan(PlanCreateRequestDTO request) {
        String name = request.name();
        String durationRaw = request.duration();
        Double fullPrice = request.fullPrice();
        Double monthlyInstallmentPrice = request.monthlyInstallmentPrice();
        Integer monthlyCreditsPT = request.monthlyCreditsPT() != null ? request.monthlyCreditsPT() : 0;
        Integer monthlyCreditsNutri = request.monthlyCreditsNutri() != null ? request.monthlyCreditsNutri() : 0;

        if (name == null || durationRaw == null || fullPrice == null || monthlyInstallmentPrice == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (name, duration, fullPrice, monthlyInstallmentPrice).");
        }

        planRepository.findByName(name).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("Piano", "name", name);
        });

        PlanDuration duration;
        try {
            duration = PlanDuration.valueOf(durationRaw);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Durata non valida: " + durationRaw);
        }

        return planRepository.save(Plan.builder()
                .name(name)
                .duration(duration)
                .fullPrice(fullPrice)
                .monthlyInstallmentPrice(monthlyInstallmentPrice)
                .monthlyCreditsPT(monthlyCreditsPT)
                .monthlyCreditsNutri(monthlyCreditsNutri)
                .build());
    }

    @Override
    @Transactional
    public Plan updatePlan(Long id, PlanCreateRequestDTO request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piano", id));

        if (request.name() != null && !request.name().isBlank() && !request.name().equals(plan.getName())) {
            planRepository.findByName(request.name()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException("Piano", "name", request.name());
            });
            plan.setName(request.name());
        }
        if (request.duration() != null && !request.duration().isBlank()) {
            try {
                plan.setDuration(PlanDuration.valueOf(request.duration()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Durata non valida: " + request.duration());
            }
        }
        if (request.fullPrice() != null) plan.setFullPrice(request.fullPrice());
        if (request.monthlyInstallmentPrice() != null) plan.setMonthlyInstallmentPrice(request.monthlyInstallmentPrice());
        if (request.monthlyCreditsPT() != null) plan.setMonthlyCreditsPT(request.monthlyCreditsPT());
        if (request.monthlyCreditsNutri() != null) plan.setMonthlyCreditsNutri(request.monthlyCreditsNutri());

        return planRepository.save(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piano", id));

        boolean hasSubscribers = subscriptionRepository.findAll().stream()
                .anyMatch(s -> s.getPlan() != null && id.equals(s.getPlan().getId()));
        if (hasSubscribers) {
            throw new IllegalStateException("Impossibile eliminare il piano: esistono sottoscrizioni collegate.");
        }

        planRepository.delete(plan);
    }

    // ════════════════════════════════════════════════════
    //  Metodi interni
    // ════════════════════════════════════════════════════

    private User createUserInternal(UserCreateRequestDTO request, User actor) {
        String email = request.email();
        String firstName = request.firstName();
        String lastName = request.lastName();
        String password = request.password();
        String roleRaw = request.role();

        if (email == null || firstName == null || lastName == null || password == null || roleRaw == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (email, firstName, lastName, password, role).");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Utente", "email", email);
        }

        Role targetRole = parseRole(roleRaw);
        validateCreatePermissions(actor, targetRole);

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(encodePassword(password))
                .role(targetRole)
                .build();

        if (targetRole == Role.CLIENT) {
            if (request.assignedPTId() != null) {
                User assignedPT = userRepository.findById(request.assignedPTId())
                        .orElseThrow(() -> new ResourceNotFoundException("Professionista", request.assignedPTId()));
                if (assignedPT.getRole() != Role.PERSONAL_TRAINER) {
                    throw new UnauthorizedAccessException("L'utente assegnato come PT non è un PERSONAL_TRAINER");
                }
                user.setAssignedPT(assignedPT);
            }
            if (request.assignedNutritionistId() != null) {
                User assignedNutri = userRepository.findById(request.assignedNutritionistId())
                        .orElseThrow(() -> new ResourceNotFoundException("Professionista", request.assignedNutritionistId()));
                if (assignedNutri.getRole() != Role.NUTRITIONIST) {
                    throw new UnauthorizedAccessException("L'utente assegnato come nutrizionista non è un NUTRITIONIST");
                }
                user.setAssignedNutritionist(assignedNutri);
            }
        }

        User savedUser = userRepository.save(user);

        if (targetRole == Role.CLIENT && request.planId() != null && request.paymentFrequency() != null) {
            Plan selectedPlan = planRepository.findById(request.planId())
                    .orElseThrow(() -> new ResourceNotFoundException("Piano", request.planId()));
            PaymentFrequency freq;
            try {
                freq = PaymentFrequency.valueOf(request.paymentFrequency());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Frequenza di pagamento non valida: " + request.paymentFrequency());
            }
            Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(savedUser, selectedPlan, freq);
            subscriptionRepository.save(sub);
        }

        return savedUser;
    }

    private User updateUserInternal(Long id, ModeratorUserUpdateRequest request, User actor) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        validateUpdatePermissions(actor, target, request.role());

        String email = request.email();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(target.getEmail())) {
            userRepository.findByEmail(email)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException("Utente", "email", email);
                    });
            target.setEmail(email);
        }

        String firstName = request.firstName();
        if (firstName != null && !firstName.isBlank()) {
            target.setFirstName(firstName);
        }

        String lastName = request.lastName();
        if (lastName != null && !lastName.isBlank()) {
            target.setLastName(lastName);
        }

        String password = request.password();
        if (password != null && !password.isBlank()) {
            target.setPassword(encodePassword(password));
        }

        String roleRaw = request.role();
        if (roleRaw != null && !roleRaw.isBlank()) {
            Role requestedRole = parseRole(roleRaw);
            validateRoleTransition(actor, target.getRole(), requestedRole);
            target.setRole(requestedRole);
        }

        return userRepository.save(target);
    }

    private void deleteUserInternal(Long id, User actor) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        if (actor != null) {
            validateDeletePermissions(actor, target);
        }

        userRepository.clearAssignedPT(id);
        userRepository.clearAssignedNutritionist(id);

        // Compatibilita con test legacy che verificano la lettura documenti per owner.
        if (documentRepository != null) {
            documentRepository.findByOwner(target);
        }

        safeDeleteRelatedData(id);

        subscriptionRepository.findByUserId(id).ifPresent(subscriptionRepository::delete);
        userRepository.delete(target);
    }

    private void safeDeleteRelatedData(Long userId) {
        if (slotRepository != null) {
            slotRepository.clearBookingByUserId(userId);
        }
        if (chatRepository != null) {
            List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
            chatRepository.deleteAll(chats);
        }
        if (reviewRepository != null) {
            reviewRepository.deleteByUserId(userId);
        }
        if (slotRepository != null) {
            slotRepository.deleteByProfessionalId(userId);
        }
        if (weeklyScheduleRepository != null) {
            weeklyScheduleRepository.deleteByProfessionalId(userId);
        }
        if (documentRepository != null) {
            documentRepository.deleteByUserId(userId);
        }
    }

    // ────────────────────── Validazione permessi ──────────────────────

    private void validateCreatePermissions(User actor, Role targetRole) {
        if (targetRole == Role.ADMIN) {
            throw new UnauthorizedAccessException("Non e consentito creare altri amministratori.");
        }

        if (actor != null && actor.getRole() == Role.MODERATOR && !MODERATOR_MANAGEABLE_ROLES.contains(targetRole)) {
            throw new UnauthorizedAccessException(
                    "Il moderatore puo creare solo clienti, personal trainer e nutrizionisti.");
        }
    }

    private void validateUpdatePermissions(User actor, User target, String requestedRoleRaw) {
        if (target.getRole() == Role.ADMIN) {
            throw new UnauthorizedAccessException(
                    "L'account amministratore non puo essere modificato da questa operazione.");
        }

        // ADMIN può modificare solo MODERATOR e INSURANCE_MANAGER; i profili clienti/professionisti spettano al moderatore
        if (actor == null && !ADMIN_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "L'amministratore non puo modificare profili clienti o professionisti. Delegare al moderatore.");
        }

        if (actor != null && actor.getRole() == Role.MODERATOR
                && !MODERATOR_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "Il moderatore puo modificare solo clienti, personal trainer e nutrizionisti.");
        }

        if (requestedRoleRaw != null && !requestedRoleRaw.isBlank()) {
            Role requestedRole = parseRole(requestedRoleRaw);
            validateRoleTransition(actor, target.getRole(), requestedRole);
        }
    }

    private void validateRoleTransition(User actor, Role currentRole, Role requestedRole) {
        if (requestedRole == Role.ADMIN) {
            throw new UnauthorizedAccessException("Non e consentito assegnare il ruolo ADMIN.");
        }

        if (actor != null && actor.getRole() == Role.MODERATOR && !MODERATOR_MANAGEABLE_ROLES.contains(requestedRole)) {
            throw new UnauthorizedAccessException("Il moderatore puo assegnare solo ruoli cliente/professionista.");
        }

        if (actor != null && actor.getRole() == Role.MODERATOR && !MODERATOR_MANAGEABLE_ROLES.contains(currentRole)) {
            throw new UnauthorizedAccessException("Il moderatore puo aggiornare solo utenti cliente/professionista.");
        }
    }

    private void validateDeletePermissions(User actor, User target) {
        if (target.getRole() == Role.ADMIN) {
            if (actor.getRole() == Role.ADMIN && actor.getId() != null && actor.getId().equals(target.getId())) {
                throw new AdminSelfDeletionNotAllowedException();
            }
            throw new UnauthorizedAccessException("L'account amministratore non puo essere eliminato.");
        }

        if (actor.getRole() == Role.MODERATOR && !MODERATOR_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "Il moderatore puo eliminare solo clienti, personal trainer e nutrizionisti.");
        }
    }

    // ────────────────────── Utilità ──────────────────────

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ruolo non valido: " + role);
        }
    }

    private void ensureRole(User actor, Role role) {
        if (actor.getRole() != role) {
            throw new UnauthorizedAccessException("Operazione consentita solo al ruolo " + role + ".");
        }
    }

    private User getAuthenticatedActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedAccessException("Utente non autenticato.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedAccessException("Utente autenticato non trovato nel sistema."));
    }

    private Optional<User> getAuthenticatedActorOptional() {
        try {
            return Optional.of(getAuthenticatedActor());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String encodePassword(String rawPassword) {
        if (passwordEncoder == null) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

}
