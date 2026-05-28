package com.project.tesi.dto.response;

/**
 * DTO di risposta per un piano di abbonamento con prezzi e crediti mensili.
 */
public class PlanResponseDTO {

    private Long id;
    private String name;
    private String duration;
    private Double fullPrice;
    private Double monthlyInstallmentPrice;
    private Integer monthlyCreditsPT;
    private Integer monthlyCreditsNutri;


    private PlanResponseDTO(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.duration = b.duration;
        this.fullPrice = b.fullPrice;
        this.monthlyInstallmentPrice = b.monthlyInstallmentPrice;
        this.monthlyCreditsPT = b.monthlyCreditsPT;
        this.monthlyCreditsNutri = b.monthlyCreditsNutri;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDuration() { return duration; }
    public Double getFullPrice() { return fullPrice; }
    public Double getMonthlyInstallmentPrice() { return monthlyInstallmentPrice; }
    public Integer getMonthlyCreditsPT() { return monthlyCreditsPT; }
    public Integer getMonthlyCreditsNutri() { return monthlyCreditsNutri; }

    public static class Builder {
        private Long id;
        private String name;
        private String duration;
        private Double fullPrice;
        private Double monthlyInstallmentPrice;
        private Integer monthlyCreditsPT;
        private Integer monthlyCreditsNutri;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder duration(String duration) { this.duration = duration; return this; }
        public Builder fullPrice(Double fullPrice) { this.fullPrice = fullPrice; return this; }
        public Builder monthlyInstallmentPrice(Double v) { this.monthlyInstallmentPrice = v; return this; }
        public Builder monthlyCreditsPT(Integer v) { this.monthlyCreditsPT = v; return this; }
        public Builder monthlyCreditsNutri(Integer v) { this.monthlyCreditsNutri = v; return this; }

        public PlanResponseDTO build() { return new PlanResponseDTO(this); }
    }
}
