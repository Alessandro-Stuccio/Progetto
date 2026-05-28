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

/**
 * Entità JPA per un piano di abbonamento offerto dalla piattaforma.
 *
 * <p>I piani hanno durata semestrale o annuale ({@code PlanDuration}) e supportano due modalità
 * di pagamento: unica soluzione ({@code fullPrice}) oppure rate mensili ({@code monthlyInstallmentPrice}).
 *
 * <p>I crediti mensili distinguono le due tipologie di professionista:
 * {@code monthlyCreditsPT} per le sessioni con personal trainer e
 * {@code monthlyCreditsNutri} per le sessioni con nutrizionista.
 * Esempio: piano Basic → 1 credito PT + 1 credito nutrizionista al mese;
 * piano Premium → 2 crediti PT + 2 crediti nutrizionista al mese.
 *
 * <p>Vincoli JPA rilevanti:
 * <ul>
 *   <li>Vincolo unico su {@code name} — ogni piano deve avere un nome distinto.</li>
 * </ul>
 */
@Entity
@Table(name = "plans", uniqueConstraints = {
        @UniqueConstraint(name = "uq_plan_name", columnNames = {"name"})
})
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome univoco del piano (es. "Basic Semestrale", "Premium Annuale"). */
    @Column(nullable = false)
    private String name;

    /** Durata del piano: {@code SEMESTRALE} (6 mesi) o {@code ANNUALE} (12 mesi). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    /** Prezzo totale in caso di pagamento in un'unica soluzione. */
    @Column(nullable = false)
    private Double fullPrice;

    /** Importo della singola rata mensile in caso di pagamento rateale. */
    @Column(nullable = false)
    private Double monthlyInstallmentPrice;

    /** Numero di crediti mensili utilizzabili per prenotare sessioni con un personal trainer. */
    private int monthlyCreditsPT;

    /** Numero di crediti mensili utilizzabili per prenotare sessioni con un nutrizionista. */
    private int monthlyCreditsNutri;

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
