package com.project.tesi.mapper;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione tra {@link Subscription} e {@link SubscriptionResponse},
 * e per la costruzione di un abbonamento a partire da una registrazione utente
 * o da un'assegnazione amministrativa.
 */
@Component
public class SubscriptionMapper {

    /**
     * Costruisce un nuovo {@link Subscription} a partire dai dati di registrazione.
     * Delega il calcolo di date, rate e crediti iniziali a {@link #buildSubscription}.
     *
     * @param request la richiesta di registrazione contenente la frequenza di pagamento
     * @param user    l'utente titolare dell'abbonamento
     * @param plan    il piano sottoscritto
     * @return la nuova entità {@link Subscription}, o {@code null} se un parametro è {@code null}
     */
    public Subscription toSubscription(RegisterRequest request, User user, Plan plan) {
        if (request == null || user == null || plan == null) return null;
        return buildSubscription(user, plan, request.paymentFrequency());
    }

    /**
     * Costruisce un nuovo {@link Subscription} a partire da dati forniti dall'amministratore.
     * Equivalente a {@link #toSubscription} ma accetta direttamente la frequenza di pagamento
     * senza una richiesta di registrazione.
     *
     * @param user             l'utente titolare
     * @param plan             il piano sottoscritto
     * @param paymentFrequency la frequenza di pagamento scelta
     * @return la nuova entità {@link Subscription}
     */
    public Subscription toSubscriptionFromAdmin(User user, Plan plan, PaymentFrequency paymentFrequency) {
        return buildSubscription(user, plan, paymentFrequency);
    }

    /**
     * Converte una {@link Subscription} in {@link SubscriptionResponse}.
     *
     * @param s l'abbonamento da convertire
     * @return il DTO di risposta, o {@code null} se l'abbonamento è {@code null}
     */
    public SubscriptionResponse toResponse(Subscription s) {
        if (s == null) return null;
        Plan plan = s.getPlan();
        Double monthlyPrice = plan != null ? plan.getMonthlyInstallmentPrice() : null;
        return SubscriptionResponse.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .userName(s.getUser() != null ? s.getUser().getFullName() : null)
                .planName(plan != null ? plan.getName() : null)
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .active(s.isActive())
                .currentCreditsPT(s.getCurrentCreditsPT())
                .currentCreditsNutri(s.getCurrentCreditsNutri())
                .monthlyPrice(monthlyPrice)
                .build();
    }

    /**
     * Converte una lista di {@link Subscription} in una lista di {@link SubscriptionResponse}.
     *
     * @param subscriptions lista degli abbonamenti
     * @return lista dei DTO di risposta
     */
    public List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Metodo privato che costruisce l'entità {@link Subscription} calcolando:
     * data di inizio (oggi), data di fine (in base alla durata del piano),
     * numero di rate totali ({@code 1} per pagamento unico, altrimenti pari ai mesi),
     * data del prossimo pagamento e crediti iniziali PT e Nutri dal piano.
     *
     * @param user             l'utente titolare
     * @param plan             il piano sottoscritto
     * @param paymentFrequency la frequenza di pagamento
     * @return la nuova entità {@link Subscription} pronta per il salvataggio
     */
    private Subscription buildSubscription(User user, Plan plan, PaymentFrequency paymentFrequency) {
        LocalDate startDate = LocalDate.now();
        int months = plan.getDuration().getMonths();
        LocalDate endDate = startDate.plusMonths(months);
        int totalInstallments = paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? 1 : months;

        return Subscription.builder()
                .user(user)
                .plan(plan)
                .paymentFrequency(paymentFrequency)
                .installmentsPaid(1)
                .totalInstallments(totalInstallments)
                .nextPaymentDate(paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? null : startDate.plusMonths(1))
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .currentCreditsPT(plan.getMonthlyCreditsPT())
                .currentCreditsNutri(plan.getMonthlyCreditsNutri())
                .lastRenewalDate(startDate)
                .build();
    }
}
