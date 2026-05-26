package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.SubscriptionFacadeImpl;
import com.project.tesi.model.Slot;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionFacadeTest {

    @Mock private SubscriptionService subscriptionService;

    private SubscriptionFacadeImpl subscriptionFacade;

    @BeforeEach
    void setUp() {
        subscriptionFacade = new SubscriptionFacadeImpl(subscriptionService);
    }

    @Test
    @DisplayName("getSubscriptionStatus — delega a SubscriptionService")
    void getSubscriptionStatus_delegates() {
        Subscription expected = new Subscription();
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(expected);

        Subscription result = subscriptionFacade.getSubscriptionStatus(1L);

        assertThat(result).isSameAs(expected);
        verify(subscriptionService).getSubscriptionStatus(1L);
    }

    @Test
    @DisplayName("activateSubscription — delega a SubscriptionService")
    void activateSubscription_delegates() {
        PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Subscription expected = new Subscription();
        when(subscriptionService.activateSubscription(req, 1L)).thenReturn(expected);

        Subscription result = subscriptionFacade.activateSubscription(req, 1L);

        assertThat(result).isSameAs(expected);
        verify(subscriptionService).activateSubscription(req, 1L);
    }

    @Test
    @DisplayName("deductCredits — delega a SubscriptionService")
    void deductCredits_delegates() {
        User pt = User.builder().id(2L).email("pt@test.com").password("password123")
                .firstName("PT").lastName("One").role(Role.PERSONAL_TRAINER).build();
        User client = User.builder().id(1L).email("c@test.com").password("password123")
                .firstName("C").lastName("L").role(Role.CLIENT).build();
        Slot slot = Slot.builder().id(10L).professional(pt).bookedBy(client)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(30)).build();

        subscriptionFacade.deductCredits(slot);

        verify(subscriptionService).deductCredits(slot);
    }

    @Test
    @DisplayName("refundCreditsIfActive — delega a SubscriptionService")
    void refundCreditsIfActive_delegates() {
        User client = User.builder().id(1L).email("c@test.com").password("password123")
                .firstName("C").lastName("L").role(Role.CLIENT).build();

        subscriptionFacade.refundCreditsIfActive(client, Role.PERSONAL_TRAINER);

        verify(subscriptionService).refundCreditsIfActive(client, Role.PERSONAL_TRAINER);
    }

    @Test
    @DisplayName("deleteByUser — delega a SubscriptionService")
    void deleteByUser_delegates() {
        subscriptionFacade.deleteByUser(1L);

        verify(subscriptionService).deleteByUser(1L);
    }
}
