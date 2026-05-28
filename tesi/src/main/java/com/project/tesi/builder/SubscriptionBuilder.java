package com.project.tesi.builder;

import com.project.tesi.enums.PaymentFrequency;
import java.time.LocalDate;
import com.project.tesi.model.*;


/**
 * Builder per l'entità Subscription. Fluent interface per la costruzione di un abbonamento utente.
 */
public interface SubscriptionBuilder {
    SubscriptionBuilder id(Long id);
    SubscriptionBuilder user(User user);
    SubscriptionBuilder plan(Plan plan);
    SubscriptionBuilder paymentFrequency(PaymentFrequency paymentFrequency);
    SubscriptionBuilder installmentsPaid(int installmentsPaid);
    SubscriptionBuilder totalInstallments(int totalInstallments);
    SubscriptionBuilder nextPaymentDate(LocalDate nextPaymentDate);
    SubscriptionBuilder startDate(LocalDate startDate);
    SubscriptionBuilder endDate(LocalDate endDate);
    SubscriptionBuilder active(boolean active);
    SubscriptionBuilder currentCreditsPT(int currentCreditsPT);
    SubscriptionBuilder currentCreditsNutri(int currentCreditsNutri);
    SubscriptionBuilder lastRenewalDate(LocalDate lastRenewalDate);
    Subscription build();
}
