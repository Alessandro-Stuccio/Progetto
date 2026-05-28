package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.facade.AdminFacade;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.ChatService;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione di {@link com.project.tesi.facade.AdminFacade}.
 * Estende {@link ModeratorFacadeImpl} aggiungendo gestione dei piani di abbonamento
 * e aggregazione delle statistiche globali della piattaforma.
 */
@Component
public class AdminFacadeImpl extends ModeratorFacadeImpl implements AdminFacade {

    private final PlanService planService;
    private final SlotService slotService;
    private final PlanMapper planMapper;

    public AdminFacadeImpl(ChatService chatService,
                           UserService userService,
                           SubscriptionService subscriptionService,
                           UserMapper userMapper,
                           SubscriptionMapper subscriptionMapper,
                           PlanService planService,
                           SlotService slotService,
                           PlanMapper planMapper,
                           SubscriptionFacade subscriptionFacade) {
        super(chatService, userService, subscriptionService, userMapper, subscriptionMapper, planService, subscriptionFacade);
        this.planService = planService;
        this.slotService = slotService;
        this.planMapper = planMapper;
    }

    /**
     * Crea un nuovo piano di abbonamento.
     * Valida la presenza di tutti i campi obbligatori e l'unicità del nome
     * prima di persistere il piano tramite {@link com.project.tesi.service.PlanService}.
     *
     * @param request dati del piano da creare
     * @return DTO del piano creato
     * @throws ResourceAlreadyExistsException se esiste già un piano con lo stesso nome
     * @throws IllegalArgumentException se campi obbligatori mancano o la durata è non valida
     */
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

    /**
     * Aggiorna un piano esistente.
     * Verifica l'unicità del nuovo nome (se cambiato) e applica le modifiche
     * tramite {@link com.project.tesi.service.PlanService}.
     *
     * @param id      identificativo del piano da aggiornare
     * @param request nuovi dati da applicare
     * @return DTO del piano aggiornato
     * @throws ResourceAlreadyExistsException se il nuovo nome è già in uso
     * @throws IllegalArgumentException se la durata fornita non è valida
     */
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

    /**
     * Elimina un piano di abbonamento.
     * Verifica che non esistano abbonamenti attivi collegati al piano
     * prima di procedere con l'eliminazione; in caso contrario lancia
     * {@link IllegalStateException}.
     *
     * @param id identificativo del piano da eliminare
     * @throws IllegalStateException se esistono sottoscrizioni attive sul piano
     */
    @Override
    @Transactional
    public void deletePlan(Long id) {
        if (subscriptionService.hasSubscribersByPlan(id)) {
            throw new IllegalStateException("Impossibile eliminare il piano: esistono sottoscrizioni collegate.");
        }
        planService.deletePlan(id);
    }

    /**
     * Restituisce le statistiche globali della piattaforma.
     * Aggrega dati da {@link com.project.tesi.service.UserService},
     * {@link com.project.tesi.service.SubscriptionService},
     * {@link com.project.tesi.service.SlotService} e
     * {@link com.project.tesi.service.PlanService} per costruire
     * un {@link com.project.tesi.dto.response.stats.AdminStatsResponse} comprensivo di:
     * utenti per ruolo, trend mensile iscrizioni, popolarità piani,
     * statistiche crediti, revenue stimata e carico di lavoro dei professionisti.
     *
     * @return risposta aggregata con tutte le statistiche admin
     */
    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        List<User> allUsers = userService.findAll();
        List<Subscription> allSubs = subscriptionService.getAllSubscriptions();
        List<Subscription> activeSubs = allSubs.stream().filter(Subscription::isActive).toList();
        List<Plan> allPlans = planService.getAllPlans();
        List<Slot> allBooked = slotService.getAllBookedSlots();

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
            usersPerMonth.add(AdminStatsResponse.MonthlyUserCount.builder()
                    .month(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN))
                    .year(ym.getYear())
                    .count(count)
                    .build());
        }

        Map<String, Long> subsByPlan = activeSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getPlan().getName(), Collectors.counting()));
        long totalActiveSubs = activeSubs.size();
        List<AdminStatsResponse.PlanPopularityItem> planPopularity = allPlans.stream()
                .map(p -> {
                    long cnt = subsByPlan.getOrDefault(p.getName(), 0L);
                    return AdminStatsResponse.PlanPopularityItem.builder()
                            .name(p.getName())
                            .activeCount(cnt)
                            .percentage(totalActiveSubs > 0 ? Math.round((cnt * 100.0) / totalActiveSubs) : 0)
                            .monthlyPrice(p.getMonthlyInstallmentPrice())
                            .fullPrice(p.getFullPrice())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getActiveCount(), a.getActiveCount()))
                .collect(Collectors.toList());

        int ptAvail = 0, nutriAvail = 0, ptMax = 0, nutriMax = 0;
        for (Subscription s : activeSubs) {
            ptAvail    += s.getCurrentCreditsPT();
            nutriAvail += s.getCurrentCreditsNutri();
            ptMax      += s.getPlan().getMonthlyCreditsPT();
            nutriMax   += s.getPlan().getMonthlyCreditsNutri();
        }
        AdminStatsResponse.CreditsStats credits = AdminStatsResponse.CreditsStats.builder()
                .ptAvailable(ptAvail)
                .ptTotal(ptMax)
                .ptConsumed(ptMax - ptAvail)
                .ptPercentUsed(ptMax > 0 ? Math.round(((ptMax - ptAvail) * 100.0) / ptMax) : 0)
                .nutriAvailable(nutriAvail)
                .nutriTotal(nutriMax)
                .nutriConsumed(nutriMax - nutriAvail)
                .nutriPercentUsed(nutriMax > 0 ? Math.round(((nutriMax - nutriAvail) * 100.0) / nutriMax) : 0)
                .build();

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
                    return AdminStatsResponse.ProfessionalWorkloadItem.builder()
                            .name(pro.getFullName())
                            .role(pro.getRole().name())
                            .clientCount(clientCount)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getClientCount(), a.getClientCount()))
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .usersByRole(usersByRole)
                .totalUsers(allUsers.size())
                .usersPerMonth(usersPerMonth)
                .planPopularity(planPopularity)
                .totalActiveSubscriptions(totalActiveSubs)
                .totalSubscriptions(allSubs.size())
                .credits(credits)
                .monthlyRevenue(monthlyRev)
                .yearlyRevenue(yearlyRev)
                .bookingsThisMonth(bookingsThisMonth)
                .bookingsTotal(allBooked.size())
                .professionalWorkload(proWorkload)
                .build();
    }
}
