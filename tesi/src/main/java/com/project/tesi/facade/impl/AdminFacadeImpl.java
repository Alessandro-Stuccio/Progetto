package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.exception.user.AdminSelfDeletionNotAllowedException;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.AdminService;
import com.project.tesi.service.AdminStatsService;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.ReviewService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminFacadeImpl extends AbstractUserManagementFacade implements AdminFacade {

    private static final Set<Role> ADMIN_MANAGEABLE_ROLES =
            EnumSet.of(Role.MODERATOR, Role.INSURANCE_MANAGER);

    private final AdminStatsService adminStatsService;
    private final PlanService planService;
    private final SlotService slotService;
    private final ChatService chatService;
    private final ReviewService reviewService;
    private final DocumentService documentService;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanMapper planMapper;

    public AdminFacadeImpl(AdminService adminService,
                           AdminStatsService adminStatsService,
                           UserService userService,
                           SubscriptionService subscriptionService,
                           PlanService planService,
                           SlotService slotService,
                           ChatService chatService,
                           ReviewService reviewService,
                           DocumentService documentService,
                           UserMapper userMapper,
                           SubscriptionMapper subscriptionMapper,
                           PlanMapper planMapper) {
        super(adminService, userService, subscriptionService, userMapper);
        this.adminStatsService = adminStatsService;
        this.planService = planService;
        this.slotService = slotService;
        this.chatService = chatService;
        this.reviewService = reviewService;
        this.documentService = documentService;
        this.subscriptionMapper = subscriptionMapper;
        this.planMapper = planMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers().stream()
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequestDTO request) {
        Role targetRole = parseRole(request.role());
        if (targetRole == Role.ADMIN) {
            throw new UnauthorizedAccessException("Non è possibile creare un utente con ruolo ADMIN.");
        }
        return buildAndSaveUser(request, targetRole);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request) {
        User target = userService.getUserById(id);

        if (target.getRole() == Role.ADMIN) {
            throw new UnauthorizedAccessException("Non è possibile modificare un utente ADMIN.");
        }
        if (!ADMIN_MANAGEABLE_ROLES.contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "L'admin non può modificare utenti con ruolo " + target.getRole() + " da questo endpoint.");
        }

        if (request.role() != null && !request.role().isBlank()) {
            Role newRole = parseRole(request.role());
            if (newRole == Role.ADMIN) {
                throw new UnauthorizedAccessException("Non è possibile assegnare il ruolo ADMIN.");
            }
            if (!ADMIN_MANAGEABLE_ROLES.contains(newRole)) {
                throw new UnauthorizedAccessException(
                        "L'admin può assegnare solo i ruoli: " + ADMIN_MANAGEABLE_ROLES);
            }
        }

        String email = request.email();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(target.getEmail())) {
            if (adminService.existsUserByEmailExcluding(email, id)) {
                throw new ResourceAlreadyExistsException("Utente", "email", email);
            }
            target.setEmail(email);
        }

        applyUserUpdates(target, request);

        return userMapper.toAdminResponse(adminService.saveUser(target));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        Optional<User> actorOpt = getAuthenticatedUserOptional();
        User target = userService.getUserById(id);

        if (target.getRole() == Role.ADMIN) {
            if (actorOpt.isPresent() && actorOpt.get().getId().equals(target.getId())) {
                throw new AdminSelfDeletionNotAllowedException();
            }
            throw new UnauthorizedAccessException("Non è possibile eliminare un utente ADMIN.");
        }

        userService.clearAssignedPT(id);
        userService.clearAssignedNutritionist(id);
        slotService.clearBookingsByUser(id);
        slotService.deleteSlotsByProfessional(id);
        slotService.deleteSchedulesByProfessional(id);
        chatService.deleteChatsForUser(id);
        reviewService.deleteByUser(id);
        documentService.deleteByUser(id);
        subscriptionService.deleteByUser(id);
        adminService.deleteUser(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return adminService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        if (pt < 0 || nutri < 0) {
            throw new IllegalArgumentException("I crediti non possono essere negativi.");
        }
        return subscriptionMapper.toResponse(adminService.updateSubscriptionCredits(id, pt, nutri));
    }

    @Override
    @Transactional
    public PlanResponseDTO createPlan(PlanCreateRequestDTO request) {
        String name = request.name();
        String durationRaw = request.duration();
        Double fullPrice = request.fullPrice();
        Double monthlyInstallmentPrice = request.monthlyInstallmentPrice();

        if (name == null || durationRaw == null || fullPrice == null || monthlyInstallmentPrice == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (name, duration, fullPrice, monthlyInstallmentPrice).");
        }

        if (planService.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Piano", "name", name);
        }

        Plan plan;
        try {
            plan = planMapper.toPlan(request);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Durata non valida: " + durationRaw);
        }

        return planMapper.toResponse(planService.createPlan(plan));
    }

    @Override
    @Transactional
    public PlanResponseDTO updatePlan(Long id, PlanCreateRequestDTO request) {
        Plan plan = planService.getPlanById(id);

        if (request.name() != null && !request.name().isBlank() && !request.name().equals(plan.getName())) {
            if (planService.existsByName(request.name())) {
                throw new ResourceAlreadyExistsException("Piano", "name", request.name());
            }
        }

        try {
            planMapper.updatePlanFromRequest(request, plan);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Durata non valida: " + request.duration());
        }

        return planMapper.toResponse(planService.createPlan(plan));
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        if (planService.hasSubscribers(id)) {
            throw new IllegalStateException("Impossibile eliminare il piano: esistono sottoscrizioni collegate.");
        }
        planService.deletePlan(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        List<User> allUsers = adminService.getAllUsers();
        List<Subscription> allSubs = adminService.getAllSubscriptions();
        List<Subscription> activeSubs = allSubs.stream().filter(Subscription::isActive).collect(Collectors.toList());
        List<Plan> allPlans = adminStatsService.getAllPlans();
        List<Slot> allBooked = adminStatsService.getAllBookedSlots();

        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));

        List<AdminStatsResponse.MonthlyUserCount> usersPerMonth = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            long count = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> {
                        LocalDate created = u.getCreatedAt().toLocalDate();
                        return !created.isBefore(start) && !created.isAfter(end);
                    })
                    .count();
            usersPerMonth.add(new AdminStatsResponse.MonthlyUserCount(
                    ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN),
                    ym.getYear(),
                    count));
        }

        Map<String, Long> subsByPlan = activeSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getPlan().getName(), Collectors.counting()));
        long totalActiveSubs = activeSubs.size();
        List<AdminStatsResponse.PlanPopularityItem> planPopularity = allPlans.stream()
                .map(p -> {
                    long cnt = subsByPlan.getOrDefault(p.getName(), 0L);
                    return new AdminStatsResponse.PlanPopularityItem(
                            p.getName(), cnt,
                            totalActiveSubs > 0 ? Math.round((cnt * 100.0) / totalActiveSubs) : 0,
                            p.getMonthlyInstallmentPrice(), p.getFullPrice());
                })
                .sorted((a, b) -> Long.compare(b.activeCount(), a.activeCount()))
                .collect(Collectors.toList());

        int ptAvail = 0, nutriAvail = 0, ptMax = 0, nutriMax = 0;
        for (Subscription s : activeSubs) {
            ptAvail    += s.getCurrentCreditsPT();
            nutriAvail += s.getCurrentCreditsNutri();
            ptMax      += s.getPlan().getMonthlyCreditsPT();
            nutriMax   += s.getPlan().getMonthlyCreditsNutri();
        }
        AdminStatsResponse.CreditsStats credits = new AdminStatsResponse.CreditsStats(
                ptAvail, ptMax, ptMax - ptAvail,
                ptMax > 0 ? Math.round(((ptMax - ptAvail) * 100.0) / ptMax) : 0,
                nutriAvail, nutriMax, nutriMax - nutriAvail,
                nutriMax > 0 ? Math.round(((nutriMax - nutriAvail) * 100.0) / nutriMax) : 0);

        double monthlyRevenue = activeSubs.stream()
                .mapToDouble(s -> s.getPlan().getMonthlyInstallmentPrice()).sum();
        double monthlyRev = Math.round(monthlyRevenue * 100.0) / 100.0;
        double yearlyRev  = Math.round(monthlyRevenue * 12 * 100.0) / 100.0;

        YearMonth thisMonth = YearMonth.now();
        long bookingsThisMonth = allBooked.stream()
                .filter(s -> s.getBookedAt() != null)
                .filter(s -> YearMonth.from(s.getBookedAt()).equals(thisMonth))
                .count();

        List<AdminStatsResponse.ProfessionalWorkloadItem> proWorkload = allUsers.stream()
                .filter(u -> u.getRole() == Role.PERSONAL_TRAINER || u.getRole() == Role.NUTRITIONIST)
                .map(pro -> {
                    long clientCount = pro.getRole() == Role.PERSONAL_TRAINER
                            ? allUsers.stream().filter(u -> u.getAssignedPT() != null && u.getAssignedPT().getId().equals(pro.getId())).count()
                            : allUsers.stream().filter(u -> u.getAssignedNutritionist() != null && u.getAssignedNutritionist().getId().equals(pro.getId())).count();
                    return new AdminStatsResponse.ProfessionalWorkloadItem(
                            pro.getFullName(),
                            pro.getRole().name(),
                            clientCount);
                })
                .sorted((a, b) -> Long.compare(b.clientCount(), a.clientCount()))
                .collect(Collectors.toList());

        return new AdminStatsResponse(
                usersByRole,
                allUsers.size(),
                usersPerMonth,
                planPopularity,
                totalActiveSubs,
                allSubs.size(),
                credits,
                monthlyRev,
                yearlyRev,
                bookingsThisMonth,
                allBooked.size(),
                proWorkload);
    }

    private Optional<User> getAuthenticatedUserOptional() {
        try {
            return Optional.of(getAuthenticatedUser());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
