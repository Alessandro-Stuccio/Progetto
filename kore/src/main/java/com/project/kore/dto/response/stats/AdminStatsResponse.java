package com.project.kore.dto.response.stats;

import java.util.List;
import java.util.Map;

/**
 * Tutte le statistiche aggregate che alimentano la dashboard dell'amministratore:
 * utenti, abbonamenti, crediti, ricavi, prenotazioni e carico dei professionisti.
 */
public class AdminStatsResponse {

    private Map<String, Long> usersByRole;
    private int totalUsers;
    private List<MonthlyUserCount> usersPerMonth;
    private List<PlanPopularityItem> planPopularity;
    private long totalActiveSubscriptions;
    private long totalSubscriptions;
    private CreditsStats credits;
    private double monthlyRevenue;
    private double yearlyRevenue;
    private long bookingsThisMonth;
    private long bookingsTotal;
    private List<ProfessionalWorkloadItem> professionalWorkload;


    private AdminStatsResponse(Builder b) {
        this.usersByRole = b.usersByRole;
        this.totalUsers = b.totalUsers;
        this.usersPerMonth = b.usersPerMonth;
        this.planPopularity = b.planPopularity;
        this.totalActiveSubscriptions = b.totalActiveSubscriptions;
        this.totalSubscriptions = b.totalSubscriptions;
        this.credits = b.credits;
        this.monthlyRevenue = b.monthlyRevenue;
        this.yearlyRevenue = b.yearlyRevenue;
        this.bookingsThisMonth = b.bookingsThisMonth;
        this.bookingsTotal = b.bookingsTotal;
        this.professionalWorkload = b.professionalWorkload;
    }

    public static Builder builder() { return new Builder(); }

    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public int getTotalUsers() { return totalUsers; }
    public List<MonthlyUserCount> getUsersPerMonth() { return usersPerMonth; }
    public List<PlanPopularityItem> getPlanPopularity() { return planPopularity; }
    public long getTotalActiveSubscriptions() { return totalActiveSubscriptions; }
    public long getTotalSubscriptions() { return totalSubscriptions; }
    public CreditsStats getCredits() { return credits; }
    public double getMonthlyRevenue() { return monthlyRevenue; }
    public double getYearlyRevenue() { return yearlyRevenue; }
    public long getBookingsThisMonth() { return bookingsThisMonth; }
    public long getBookingsTotal() { return bookingsTotal; }
    public List<ProfessionalWorkloadItem> getProfessionalWorkload() { return professionalWorkload; }

    public static class Builder {
        private Map<String, Long> usersByRole;
        private int totalUsers;
        private List<MonthlyUserCount> usersPerMonth;
        private List<PlanPopularityItem> planPopularity;
        private long totalActiveSubscriptions;
        private long totalSubscriptions;
        private CreditsStats credits;
        private double monthlyRevenue;
        private double yearlyRevenue;
        private long bookingsThisMonth;
        private long bookingsTotal;
        private List<ProfessionalWorkloadItem> professionalWorkload;

        public Builder usersByRole(Map<String, Long> v) { this.usersByRole = v; return this; }
        public Builder totalUsers(int v) { this.totalUsers = v; return this; }
        public Builder usersPerMonth(List<MonthlyUserCount> v) { this.usersPerMonth = v; return this; }
        public Builder planPopularity(List<PlanPopularityItem> v) { this.planPopularity = v; return this; }
        public Builder totalActiveSubscriptions(long v) { this.totalActiveSubscriptions = v; return this; }
        public Builder totalSubscriptions(long v) { this.totalSubscriptions = v; return this; }
        public Builder credits(CreditsStats v) { this.credits = v; return this; }
        public Builder monthlyRevenue(double v) { this.monthlyRevenue = v; return this; }
        public Builder yearlyRevenue(double v) { this.yearlyRevenue = v; return this; }
        public Builder bookingsThisMonth(long v) { this.bookingsThisMonth = v; return this; }
        public Builder bookingsTotal(long v) { this.bookingsTotal = v; return this; }
        public Builder professionalWorkload(List<ProfessionalWorkloadItem> v) { this.professionalWorkload = v; return this; }

        public AdminStatsResponse build() { return new AdminStatsResponse(this); }
    }

    /** Iscrizioni di un singolo mese, per il grafico di crescita utenti. */
    public static class MonthlyUserCount {
        private String month;
        private int year;
        private long count;


        private MonthlyUserCount(Builder b) {
            this.month = b.month;
            this.year = b.year;
            this.count = b.count;
        }

        public static Builder builder() { return new Builder(); }

        public String getMonth() { return month; }
        public int getYear() { return year; }
        public long getCount() { return count; }

        public static class Builder {
            private String month;
            private int year;
            private long count;

            public Builder month(String month) { this.month = month; return this; }
            public Builder year(int year) { this.year = year; return this; }
            public Builder count(long count) { this.count = count; return this; }

            public MonthlyUserCount build() { return new MonthlyUserCount(this); }
        }
    }

    /** Quota di un piano sul totale degli abbonamenti attivi; percentage è la percentuale calcolata. */
    public static class PlanPopularityItem {
        private String name;
        private long activeCount;
        private long percentage;
        private double monthlyPrice;
        private double fullPrice;


        private PlanPopularityItem(Builder b) {
            this.name = b.name;
            this.activeCount = b.activeCount;
            this.percentage = b.percentage;
            this.monthlyPrice = b.monthlyPrice;
            this.fullPrice = b.fullPrice;
        }

        public static Builder builder() { return new Builder(); }

        public String getName() { return name; }
        public long getActiveCount() { return activeCount; }
        public long getPercentage() { return percentage; }
        public double getMonthlyPrice() { return monthlyPrice; }
        public double getFullPrice() { return fullPrice; }

        public static class Builder {
            private String name;
            private long activeCount;
            private long percentage;
            private double monthlyPrice;
            private double fullPrice;

            public Builder name(String name) { this.name = name; return this; }
            public Builder activeCount(long activeCount) { this.activeCount = activeCount; return this; }
            public Builder percentage(long percentage) { this.percentage = percentage; return this; }
            public Builder monthlyPrice(double monthlyPrice) { this.monthlyPrice = monthlyPrice; return this; }
            public Builder fullPrice(double fullPrice) { this.fullPrice = fullPrice; return this; }

            public PlanPopularityItem build() { return new PlanPopularityItem(this); }
        }
    }

    /** Crediti disponibili, totali e consumati, distinti tra PT e nutrizionista; i percentUsed sono calcolati. */
    public static class CreditsStats {
        private int ptAvailable;
        private int ptTotal;
        private int ptConsumed;
        private long ptPercentUsed;
        private int nutriAvailable;
        private int nutriTotal;
        private int nutriConsumed;
        private long nutriPercentUsed;


        private CreditsStats(Builder b) {
            this.ptAvailable = b.ptAvailable;
            this.ptTotal = b.ptTotal;
            this.ptConsumed = b.ptConsumed;
            this.ptPercentUsed = b.ptPercentUsed;
            this.nutriAvailable = b.nutriAvailable;
            this.nutriTotal = b.nutriTotal;
            this.nutriConsumed = b.nutriConsumed;
            this.nutriPercentUsed = b.nutriPercentUsed;
        }

        public static Builder builder() { return new Builder(); }

        public int getPtAvailable() { return ptAvailable; }
        public int getPtTotal() { return ptTotal; }
        public int getPtConsumed() { return ptConsumed; }
        public long getPtPercentUsed() { return ptPercentUsed; }
        public int getNutriAvailable() { return nutriAvailable; }
        public int getNutriTotal() { return nutriTotal; }
        public int getNutriConsumed() { return nutriConsumed; }
        public long getNutriPercentUsed() { return nutriPercentUsed; }

        public static class Builder {
            private int ptAvailable;
            private int ptTotal;
            private int ptConsumed;
            private long ptPercentUsed;
            private int nutriAvailable;
            private int nutriTotal;
            private int nutriConsumed;
            private long nutriPercentUsed;

            public Builder ptAvailable(int v) { this.ptAvailable = v; return this; }
            public Builder ptTotal(int v) { this.ptTotal = v; return this; }
            public Builder ptConsumed(int v) { this.ptConsumed = v; return this; }
            public Builder ptPercentUsed(long v) { this.ptPercentUsed = v; return this; }
            public Builder nutriAvailable(int v) { this.nutriAvailable = v; return this; }
            public Builder nutriTotal(int v) { this.nutriTotal = v; return this; }
            public Builder nutriConsumed(int v) { this.nutriConsumed = v; return this; }
            public Builder nutriPercentUsed(long v) { this.nutriPercentUsed = v; return this; }

            public CreditsStats build() { return new CreditsStats(this); }
        }
    }

    /** Numero di clienti seguiti da un professionista, per misurarne il carico. */
    public static class ProfessionalWorkloadItem {
        private String name;
        private String role;
        private long clientCount;


        private ProfessionalWorkloadItem(Builder b) {
            this.name = b.name;
            this.role = b.role;
            this.clientCount = b.clientCount;
        }

        public static Builder builder() { return new Builder(); }

        public String getName() { return name; }
        public String getRole() { return role; }
        public long getClientCount() { return clientCount; }

        public static class Builder {
            private String name;
            private String role;
            private long clientCount;

            public Builder name(String name) { this.name = name; return this; }
            public Builder role(String role) { this.role = role; return this; }
            public Builder clientCount(long clientCount) { this.clientCount = clientCount; return this; }

            public ProfessionalWorkloadItem build() { return new ProfessionalWorkloadItem(this); }
        }
    }
}
