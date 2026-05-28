package com.project.tesi.facade;

import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.SubscriptionFacadeImpl;
import com.project.tesi.model.Plan;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionFacadeTest {

    @Mock private SubscriptionService subscriptionService;

    private SubscriptionFacadeImpl subscriptionFacade;

    private User client;
    private Plan plan;

    @BeforeEach
    void setUp() {
        subscriptionFacade = new SubscriptionFacadeImpl(subscriptionService);
        client = User.builder().id(1L).email("c@test.com").password("password123").role(Role.CLIENT).build();
        plan = Plan.builder().id(1L).name("Basic").duration(PlanDuration.SEMESTRALE)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1).fullPrice(50.0).monthlyInstallmentPrice(10.0).build();
    }

    @Test
    @DisplayName("activateSubscription(User,Plan,Freq) — disattiva abbonamento esistente e crea nuovo")
    void activateSubscription_deactivatesExistingAndCreatesNew() {
        Subscription existing = new Subscription();
        existing.setActive(true);
        when(subscriptionService.findActiveByUser(client)).thenReturn(Optional.of(existing));
        when(subscriptionService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = subscriptionFacade.activateSubscription(client, plan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(existing.isActive()).isFalse();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUser()).isEqualTo(client);
        verify(subscriptionService, times(2)).save(any());
    }

    @Test
    @DisplayName("activateSubscription — UNICA_SOLUZIONE imposta totalInstallments=1 e nextPaymentDate=null")
    void activateSubscription_unicaSoluzione_setsInstallments() {
        when(subscriptionService.findActiveByUser(client)).thenReturn(Optional.empty());
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        when(subscriptionService.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        subscriptionFacade.activateSubscription(client, plan, PaymentFrequency.UNICA_SOLUZIONE);

        Subscription saved = captor.getValue();
        assertThat(saved.getTotalInstallments()).isEqualTo(1);
        assertThat(saved.getNextPaymentDate()).isNull();
    }

}
