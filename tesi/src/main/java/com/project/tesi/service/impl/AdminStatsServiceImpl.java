package com.project.tesi.service.impl;

import com.project.tesi.dto.response.stats.AdminStatsResponse;
import com.project.tesi.dto.response.stats.AdminStatsResponse.CreditsStats;
import com.project.tesi.dto.response.stats.AdminStatsResponse.MonthlyUserCount;
import com.project.tesi.dto.response.stats.AdminStatsResponse.PlanPopularityItem;
import com.project.tesi.dto.response.stats.AdminStatsResponse.ProfessionalWorkloadItem;
import com.project.tesi.enums.Role;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.repository.BookingRepository;
import com.project.tesi.repository.PlanRepository;
import com.project.tesi.repository.SubscriptionRepository;
import com.project.tesi.repository.UserRepository;
import com.project.tesi.service.AdminStatsService;
import org.springframework.stereotype.Service;
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
 * Implementazione del servizio per le statistiche del pannello Admin.
 */
@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingRepository bookingRepository;
    private final PlanRepository planRepository;

    public AdminStatsServiceImpl(UserRepository userRepository,
                                  SubscriptionRepository subscriptionRepository,
                                  BookingRepository bookingRepository,
                                  PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.bookingRepository = bookingRepository;
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        List<User> allUsers = userRepository.findAll();
        List<Subscription> allSubs = subscriptionRepository.findAll();
        List<Subscription> activeSubs = allSubs.stream().filter(Subscription::isActive).collect(Collectors.toList());
        List<Plan> allPlans = planRepository.findAll();

        // 1. Distribuzione utenti per ruolo
        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));

        // 2. Nuovi utenti per mese (ultimi 6 mesi)
        List<MonthlyUserCount> usersPerMonth = new ArrayList<>();
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
            usersPerMonth.add(new MonthlyUserCount(
                    ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN),
                    ym.getYear(),
                    count));
        }

        // 3. Piani più sottoscritti
        Map<String, Long> subsByPlan = activeSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getPlan().getName(), Collectors.counting()));
        long totalActiveSubs = activeSubs.size();
        List<PlanPopularityItem> planPopularity = allPlans.stream()
                .map(p -> {
                    long cnt = subsByPlan.getOrDefault(p.getName(), 0L);
                    return new PlanPopularityItem(
                            p.getName(), cnt,
                            totalActiveSubs > 0 ? Math.round((cnt * 100.0) / totalActiveSubs) : 0,
                            p.getMonthlyInstallmentPrice(), p.getFullPrice());
                })
                .sorted((a, b) -> Long.compare(b.activeCount(), a.activeCount()))
                .collect(Collectors.toList());

        // 4. Crediti
        int ptAvail = 0, nutriAvail = 0, ptMax = 0, nutriMax = 0;
        for (Subscription s : activeSubs) {
            ptAvail    += s.getCurrentCreditsPT();
            nutriAvail += s.getCurrentCreditsNutri();
            ptMax      += s.getPlan().getMonthlyCreditsPT();
            nutriMax   += s.getPlan().getMonthlyCreditsNutri();
        }
        CreditsStats credits = new CreditsStats(
                ptAvail, ptMax, ptMax - ptAvail,
                ptMax > 0 ? Math.round(((ptMax - ptAvail) * 100.0) / ptMax) : 0,
                nutriAvail, nutriMax, nutriMax - nutriAvail,
                nutriMax > 0 ? Math.round(((nutriMax - nutriAvail) * 100.0) / nutriMax) : 0);

        // 5. Fatturato stimato
        double monthlyRevenue = activeSubs.stream()
                .mapToDouble(s -> s.getPlan().getMonthlyInstallmentPrice()).sum();
        double monthlyRev = Math.round(monthlyRevenue * 100.0) / 100.0;
        double yearlyRev  = Math.round(monthlyRevenue * 12 * 100.0) / 100.0;

        // 6. Prenotazioni
        List<Booking> allBookings = bookingRepository.findAll();
        YearMonth thisMonth = YearMonth.now();
        long bookingsThisMonth = allBookings.stream()
                .filter(b -> b.getBookedAt() != null)
                .filter(b -> YearMonth.from(b.getBookedAt()).equals(thisMonth))
                .count();

        // 7. Carico professionisti
        List<ProfessionalWorkloadItem> proWorkload = allUsers.stream()
                .filter(u -> u.getRole() == Role.PERSONAL_TRAINER || u.getRole() == Role.NUTRITIONIST)
                .map(pro -> {
                    long clientCount = pro.getRole() == Role.PERSONAL_TRAINER
                            ? allUsers.stream().filter(u -> u.getAssignedPT() != null && u.getAssignedPT().getId().equals(pro.getId())).count()
                            : allUsers.stream().filter(u -> u.getAssignedNutritionist() != null && u.getAssignedNutritionist().getId().equals(pro.getId())).count();
                    return new ProfessionalWorkloadItem(
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
                allBookings.size(),
                proWorkload);
    }
}
