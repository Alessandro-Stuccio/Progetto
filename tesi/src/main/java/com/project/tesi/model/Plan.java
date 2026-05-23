package com.project.tesi.model;

import com.project.tesi.builder.PlanBuilder;
import com.project.tesi.builder.impl.PlanBuilderImpl;
import com.project.tesi.enums.PlanDuration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(name = "plans", uniqueConstraints = {
        @UniqueConstraint(name = "uq_plan_name", columnNames = {"name"})
})
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    @Column(nullable = false)
    private Double fullPrice;

    @Column(nullable = false)
    private Double monthlyInstallmentPrice;

    private int monthlyCreditsPT;
    private int monthlyCreditsNutri;
    private String insuranceCoverageDetails;

    public Plan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlanDuration getDuration() { return duration; }
    public void setDuration(PlanDuration duration) { this.duration = duration; }

    public Double getFullPrice() { return fullPrice; }
    public void setFullPrice(Double fullPrice) { this.fullPrice = fullPrice; }

    public Double getMonthlyInstallmentPrice() { return monthlyInstallmentPrice; }
    public void setMonthlyInstallmentPrice(Double monthlyInstallmentPrice) { this.monthlyInstallmentPrice = monthlyInstallmentPrice; }

    public int getMonthlyCreditsPT() { return monthlyCreditsPT; }
    public void setMonthlyCreditsPT(int monthlyCreditsPT) { this.monthlyCreditsPT = monthlyCreditsPT; }

    public int getMonthlyCreditsNutri() { return monthlyCreditsNutri; }
    public void setMonthlyCreditsNutri(int monthlyCreditsNutri) { this.monthlyCreditsNutri = monthlyCreditsNutri; }

    public String getInsuranceCoverageDetails() { return insuranceCoverageDetails; }
    public void setInsuranceCoverageDetails(String insuranceCoverageDetails) { this.insuranceCoverageDetails = insuranceCoverageDetails; }

    public static PlanBuilder builder() {
        return new PlanBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan that = (Plan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Plan{id=" + id + ", name='" + name + "', duration=" + duration + ", fullPrice=" + fullPrice + ", monthlyCreditsPT=" + monthlyCreditsPT + ", monthlyCreditsNutri=" + monthlyCreditsNutri + "}";
    }
}
