package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.facade.PlanFacade;
import com.project.tesi.mapper.PlanMapper;
import com.project.tesi.service.PlanService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
@Component
public class PlanFacadeImpl implements PlanFacade {

    private final PlanService planService;
    private final PlanMapper planMapper;

    public PlanFacadeImpl(PlanService planService, PlanMapper planMapper) {
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> getAllPlans() {
        return planMapper.toResponseList(planService.getActivePlans());
    }
}
