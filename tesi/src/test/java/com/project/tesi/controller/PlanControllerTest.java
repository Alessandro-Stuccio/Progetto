package com.project.tesi.controller;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.facade.PlanFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test unitari per {@link PlanController}.
 */
@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    @Mock private PlanFacade planFacade;

    @InjectMocks
    private PlanController planController;

    @Test
    @DisplayName("getAllPlans — restituisce 200 con lista piani")
    void getAllPlans() {
        PlanResponseDTO p = new PlanResponseDTO(1L, "Premium", "ANNUALE", 100.0, 10.0, 5, 5);
        when(planFacade.getAllPlans()).thenReturn(List.of(p));

        ResponseEntity<List<PlanResponseDTO>> response = planController.getAllPlans();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Premium");
    }
}
