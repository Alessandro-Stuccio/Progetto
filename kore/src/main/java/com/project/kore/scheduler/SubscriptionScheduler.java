package com.project.kore.scheduler;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.model.Subscription;
import com.project.kore.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Cron job che gira in background ogni notte a mezzanotte.
 * Controlla se è il primo del mese: se sì, resetta i crediti mensili di PT e Nutrizionista 
 * per tutti gli abbonamenti attivi, gestendo anche lo scatto delle rate.
 */
@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionScheduler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "${schedule.time.subscription}")
    @Transactional
    public void renewCredits() {
        List<Subscription> activeSubs = subscriptionRepository.findByActiveTrue();
        LocalDate today = LocalDate.now();

        for (Subscription sub : activeSubs) {
            try {
                // Reset crediti il primo giorno di ogni mese
                if (today.getDayOfMonth() == 1) {
                    if (sub.getPaymentFrequency() == PaymentFrequency.RATE_MENSILI) {
                        if (sub.getNextPaymentDate() != null
                                && !today.isBefore(sub.getNextPaymentDate())
                                && sub.getInstallmentsPaid() < sub.getTotalInstallments()) {

                            sub.setInstallmentsPaid(sub.getInstallmentsPaid() + 1);
                            sub.setNextPaymentDate(sub.getNextPaymentDate().plusMonths(1));
                        } else {
                            log.warn("Pagamento rateale non dovuto per l'abbonamento ID {}: salto il reset dei crediti", sub.getId());
                            continue;
                        }
                    }

                    sub.setCurrentCreditsPT(sub.getPlan().getMonthlyCreditsPT());
                    sub.setCurrentCreditsNutri(sub.getPlan().getMonthlyCreditsNutri());
                    sub.setLastRenewalDate(today);

                    subscriptionRepository.save(sub);
                }
            } catch (Exception e) {
                log.error("Errore nel rinnovo crediti per subscription ID {}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}