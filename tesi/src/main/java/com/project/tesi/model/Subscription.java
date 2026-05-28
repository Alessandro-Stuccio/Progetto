package com.project.tesi.model;

import com.project.tesi.builder.SubscriptionBuilder;
import com.project.tesi.builder.impl.SubscriptionBuilderImpl;
import com.project.tesi.enums.PaymentFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entità JPA per l'abbonamento attivo di un utente.
 * Ogni utente ha al massimo un abbonamento (vincolo unico su user_id).
 * I crediti {@code currentCreditsPT} e {@code currentCreditsNutri} vengono
 * decrementati ad ogni prenotazione e ripristinati mensilmente dallo
 * {@code SubscriptionScheduler}. Il campo {@code version} supporta
 * l'optimistic locking.
 */
@Entity
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_subscription_user", columnNames = {"user_id"})
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Versione per l'optimistic locking; incrementata automaticamente da JPA ad ogni aggiornamento. */
    @Version
    private Integer version;

    /** Utente titolare dell'abbonamento; relazione 1:1. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_user_id"))
    private User user;

    /** Piano sottoscritto (Basic o Premium); caricato in EAGER per accesso diretto ai crediti. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_plan_id"))
    private Plan plan;

    /** Modalità di pagamento: pagamento unico o a rate. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;

    /** Numero di rate già pagate (rilevante solo per pagamento rateale). */
    private int installmentsPaid;

    /** Numero totale di rate previste dal piano (rilevante solo per pagamento rateale). */
    private int totalInstallments;

    /** Data della prossima scadenza di pagamento (rata o rinnovo). */
    private LocalDate nextPaymentDate;

    /** Data di inizio validità dell'abbonamento. */
    private LocalDate startDate;

    /** Data di scadenza dell'abbonamento. */
    private LocalDate endDate;

    /** Indica se l'abbonamento è attualmente attivo. */
    private boolean active;

    /** Crediti residui per prenotare sessioni con il personal trainer nel mese corrente. */
    private int currentCreditsPT;

    /** Crediti residui per prenotare sessioni con il nutrizionista nel mese corrente. */
    private int currentCreditsNutri;

    /** Data dell'ultimo rinnovo mensile dei crediti effettuato dallo {@code SubscriptionScheduler}. */
    private LocalDate lastRenewalDate;

    public Subscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public PaymentFrequency getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(PaymentFrequency paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public int getInstallmentsPaid() { return installmentsPaid; }
    public void setInstallmentsPaid(int installmentsPaid) { this.installmentsPaid = installmentsPaid; }

    public int getTotalInstallments() { return totalInstallments; }
    public void setTotalInstallments(int totalInstallments) { this.totalInstallments = totalInstallments; }

    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getCurrentCreditsPT() { return currentCreditsPT; }
    public void setCurrentCreditsPT(int currentCreditsPT) { this.currentCreditsPT = currentCreditsPT; }

    public int getCurrentCreditsNutri() { return currentCreditsNutri; }
    public void setCurrentCreditsNutri(int currentCreditsNutri) { this.currentCreditsNutri = currentCreditsNutri; }

    public LocalDate getLastRenewalDate() { return lastRenewalDate; }
    public void setLastRenewalDate(LocalDate lastRenewalDate) { this.lastRenewalDate = lastRenewalDate; }

    public static SubscriptionBuilder builder() {
        return new SubscriptionBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subscription{id=" + id + ", paymentFrequency=" + paymentFrequency + ", active=" + active + ", currentCreditsPT=" + currentCreditsPT + ", currentCreditsNutri=" + currentCreditsNutri + "}";
    }
}
