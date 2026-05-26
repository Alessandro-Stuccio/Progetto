package com.project.tesi.facade;

import com.project.tesi.dto.request.PlanRequest;
import com.project.tesi.dto.response.*;
import com.project.tesi.dto.response.stats.ProfessionalStatsResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.impl.SecurityContextHelper;
import com.project.tesi.facade.impl.UserFacadeImpl;
import com.project.tesi.mapper.BookingMapper;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Subscription;
import com.project.tesi.model.User;
import com.project.tesi.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock private UserService userService;
    @Mock private PlanService planService;
    @Mock private ReviewService reviewService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private ChatService chatService;
    @Mock private ProfessionalStatsService professionalStatsService;
    @Mock private SlotService slotService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private BookingMapper bookingMapper;
    @Mock private EmailService emailService;
    @Mock private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    @DisplayName("activateSubscription — chiama il service e mappa a DTO")
    void activateSubscription() {
        PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Subscription sub = new Subscription();
        sub.setId(1L);
        SubscriptionResponse expected = SubscriptionResponse.builder().id(1L).build();
        when(subscriptionService.activateSubscription(req, 1L)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(expected);

        assertThat(userFacade.activateSubscription(req, 1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getSubscriptionStatus — chiama il service e mappa a DTO")
    void getSubscriptionStatus() {
        Subscription sub = new Subscription();
        sub.setId(1L);
        sub.setActive(true);
        SubscriptionResponse expected = SubscriptionResponse.builder().active(true).build();
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(expected);

        assertThat(userFacade.getSubscriptionStatus(1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getProfessionalStats — aggrega dati dai metodi raw di ProfessionalStatsService")
    void getProfessionalStats() {
        User pt = User.builder().id(2L).email("pt@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        when(userService.getUserById(2L)).thenReturn(pt);
        when(professionalStatsService.getTodaySlots(eq(pt), any(), any())).thenReturn(List.of());
        when(professionalStatsService.getAssignedClients(pt)).thenReturn(List.of());
        when(professionalStatsService.countDocumentsUploadedSince(eq(pt), any())).thenReturn(0);

        ProfessionalStatsResponse result = userFacade.getProfessionalStats(2L);
        assertThat(result).isNotNull();
        assertThat(result.todayBookingsCount()).isEqualTo(0);
        assertThat(result.totalClients()).isEqualTo(0);
    }

    @Test
    @DisplayName("getAdmin — restituisce il primo admin come ClientBasicInfoResponse")
    void getAdmin() {
        User admin = User.builder().id(1L).firstName("Admin").lastName("System")
                .email("admin@test.com").password("password123").role(Role.ADMIN).build();
        ClientBasicInfoResponse adminResponse = ClientBasicInfoResponse.builder()
                .id(1L).email("admin@test.com").build();
        when(userService.findByRole(Role.ADMIN)).thenReturn(List.of(admin));
        when(userMapper.toBasicInfoResponse(admin)).thenReturn(adminResponse);

        ClientBasicInfoResponse result = userFacade.getAdmin();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("getClientsForProfessional — ritorna lista clienti di un PT")
    void getClientsForProfessional() {
        User pt = User.builder().id(2L).email("pt@test.com").password("password123").role(Role.PERSONAL_TRAINER).build();
        User client = User.builder().id(3L).firstName("Luca").lastName("Bianchi")
                .email("luca@test.com").password("password123").role(Role.CLIENT).build();
        ClientBasicInfoResponse clientResponse = ClientBasicInfoResponse.builder()
                .id(3L).email("luca@test.com").build();
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userService.findByAssignedPT(pt)).thenReturn(List.of(client));
        when(userMapper.toBasicInfoResponse(client)).thenReturn(clientResponse);

        List<ClientBasicInfoResponse> result = userFacade.getClientsForProfessional(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("luca@test.com");
    }
}
