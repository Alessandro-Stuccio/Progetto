package com.project.kore.dto.response;

import java.time.LocalDate;

/**
 * L'abbonamento di un utente con i crediti PT/nutrizionista residui e il periodo di validità.
 */
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private int currentCreditsPT;
    private int currentCreditsNutri;
    private Double monthlyPrice;


    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getPlanName() { return planName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public int getCurrentCreditsPT() { return currentCreditsPT; }
    public int getCurrentCreditsNutri() { return currentCreditsNutri; }
    public Double getMonthlyPrice() { return monthlyPrice; }

    public static class Builder {
        private Long id;
        private Long userId;
        private String userName;
        private String planName;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active;
        private int currentCreditsPT;
        private int currentCreditsNutri;
        private Double monthlyPrice;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder planName(String planName) { this.planName = planName; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder currentCreditsPT(int credits) { this.currentCreditsPT = credits; return this; }
        public Builder currentCreditsNutri(int credits) { this.currentCreditsNutri = credits; return this; }
        public Builder monthlyPrice(Double price) { this.monthlyPrice = price; return this; }

        public SubscriptionResponse build() {
            SubscriptionResponse r = new SubscriptionResponse();
            r.id = this.id;
            r.userId = this.userId;
            r.userName = this.userName;
            r.planName = this.planName;
            r.startDate = this.startDate;
            r.endDate = this.endDate;
            r.active = this.active;
            r.currentCreditsPT = this.currentCreditsPT;
            r.currentCreditsNutri = this.currentCreditsNutri;
            r.monthlyPrice = this.monthlyPrice;
            return r;
        }
    }
}
