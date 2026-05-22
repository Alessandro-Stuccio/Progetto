package com.project.tesi.mapper;

import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.model.Plan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlanMapper {

    public PlanResponseDTO toResponse(Plan p) {
        if (p == null) return null;
        return new PlanResponseDTO(
                p.getId(),
                p.getName(),
                p.getDuration() != null ? p.getDuration().name() : null,
                p.getFullPrice(),
                p.getMonthlyInstallmentPrice(),
                p.getMonthlyCreditsPT(),
                p.getMonthlyCreditsNutri()
        );
    }

    public List<PlanResponseDTO> toResponseList(List<Plan> plans) {
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
