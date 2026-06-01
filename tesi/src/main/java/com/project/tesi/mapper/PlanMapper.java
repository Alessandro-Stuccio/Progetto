package com.project.tesi.mapper;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.model.Plan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte i piani tra entità e DTO di richiesta/risposta.
 */
@Component
public class PlanMapper {

    public PlanResponseDTO toResponse(Plan p) {
        if (p == null) return null;
        return PlanResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .duration(p.getDuration() != null ? p.getDuration().name() : null)
                .fullPrice(p.getFullPrice())
                .monthlyInstallmentPrice(p.getMonthlyInstallmentPrice())
                .monthlyCreditsPT(p.getMonthlyCreditsPT())
                .monthlyCreditsNutri(p.getMonthlyCreditsNutri())
                .active(p.isActive())
                .build();
    }

    public List<PlanResponseDTO> toResponseList(List<Plan> plans) {
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Plan toPlan(PlanCreateRequestDTO request) {
        PlanDuration duration = PlanDuration.valueOf(request.duration());
        return Plan.builder()
                .name(request.name())
                .duration(duration)
                .fullPrice(request.fullPrice())
                .monthlyInstallmentPrice(request.monthlyInstallmentPrice())
                .monthlyCreditsPT(request.monthlyCreditsPT() != null ? request.monthlyCreditsPT() : 0)
                .monthlyCreditsNutri(request.monthlyCreditsNutri() != null ? request.monthlyCreditsNutri() : 0)
                .build();
    }

    // Aggiornamento parziale: sovrascrive solo i campi valorizzati nel DTO,
    // lasciando intatti quelli null o blank.
    public void updatePlanFromRequest(PlanCreateRequestDTO request, Plan existing) {
        if (request.name() != null && !request.name().isBlank())
            existing.setName(request.name());
        if (request.duration() != null && !request.duration().isBlank())
            existing.setDuration(PlanDuration.valueOf(request.duration()));
        if (request.fullPrice() != null)
            existing.setFullPrice(request.fullPrice());
        if (request.monthlyInstallmentPrice() != null)
            existing.setMonthlyInstallmentPrice(request.monthlyInstallmentPrice());
        if (request.monthlyCreditsPT() != null)
            existing.setMonthlyCreditsPT(request.monthlyCreditsPT());
        if (request.monthlyCreditsNutri() != null)
            existing.setMonthlyCreditsNutri(request.monthlyCreditsNutri());
    }
}
