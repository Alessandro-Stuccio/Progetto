package com.project.tesi.mapper;

import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.model.User;
import com.project.tesi.repository.ReviewRepository;
import com.project.tesi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test unitari per {@link UserMapper}.
 */
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    @DisplayName("toUserResponse — utente CLIENT con professionisti assegnati")
    void toUserResponse_client() {
        User pt = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(2L).firstName("Luca").lastName("Bianchi").build();
        User nutri = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(3L).firstName("Sara").lastName("Verdi").build();
        User client = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi")
                .email("mario@test.com").role(Role.CLIENT).assignedPT(pt).assignedNutritionist(nutri).build();

        UserResponse resp = userMapper.toUserResponse(client);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getFirstName()).isEqualTo("Mario");
        assertThat(resp.getRole()).isEqualTo(Role.CLIENT);
        assertThat(resp.getAssignedPtName()).isEqualTo("Luca Bianchi");
        assertThat(resp.getAssignedNutritionistName()).isEqualTo("Sara Verdi");
        assertThat(resp.getActiveClientsCount()).isNull();
        assertThat(resp.getAverageRating()).isNull();
    }

    @Test
    @DisplayName("toUserResponse — professionista PT con rating e conteggio clienti")
    void toUserResponse_pt() {
        User pt = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(2L).firstName("Luca").lastName("Bianchi")
                .email("luca@test.com").role(Role.PERSONAL_TRAINER).build();

        when(reviewRepository.getAverageRating(2L)).thenReturn(4.5);
        when(userRepository.countByAssignedPTAndDeletedFalse(pt)).thenReturn(7L);

        UserResponse resp = userMapper.toUserResponse(pt);

        assertThat(resp.getRole()).isEqualTo(Role.PERSONAL_TRAINER);
        assertThat(resp.getAverageRating()).isEqualTo(4.5);
        assertThat(resp.getActiveClientsCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("toUserResponse — nutrizionista con rating null → default 0.0")
    void toUserResponse_nutritionist_nullRating() {
        User nutri = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(3L).firstName("Sara").lastName("Verdi")
                .email("sara@test.com").role(Role.NUTRITIONIST).build();

        when(reviewRepository.getAverageRating(3L)).thenReturn(null);
        when(userRepository.countByAssignedNutritionistAndDeletedFalse(nutri)).thenReturn(3L);

        UserResponse resp = userMapper.toUserResponse(nutri);

        assertThat(resp.getAverageRating()).isEqualTo(0.0);
        assertThat(resp.getActiveClientsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("toUserResponse — client senza professionisti assegnati")
    void toUserResponse_clientNoProfessionals() {
        User client = User.builder().email("test@test.com").password("testpass").role(com.project.tesi.enums.Role.CLIENT).id(1L).firstName("Mario").lastName("Rossi")
                .email("mario@test.com").role(Role.CLIENT).build();

        UserResponse resp = userMapper.toUserResponse(client);

        assertThat(resp.getAssignedPtName()).isNull();
        assertThat(resp.getAssignedNutritionistName()).isNull();
    }

    @Test
    @DisplayName("toUser — converte RegisterRequest in User con ruolo CLIENT")
    void toUser_success() {
        RegisterRequest req = new RegisterRequest("Mario", "Rossi", "mario@test.com", "secretpass", null, null, "pic.jpg", null, null);

        User user = userMapper.toUser(req);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Mario");
        assertThat(user.getRole()).isEqualTo(Role.CLIENT);
        assertThat(user.getEmail()).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("toUser — null restituisce null")
    void toUser_null() {
        assertThat(userMapper.toUser(null)).isNull();
    }
}

