package com.project.tesi.mapper;

import com.project.tesi.dto.request.PlanCreateRequestDTO;
import com.project.tesi.dto.response.PlanResponseDTO;
import com.project.tesi.enums.PlanDuration;
import com.project.tesi.model.Plan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per la conversione tra {@link Plan} e i relativi DTO di richiesta/risposta.
 */
@Component
public class PlanMapper {

    /**
     * Converte un {@link Plan} in {@link PlanResponseDTO}.
     *
     * @param p il piano da convertire
     * @return il DTO di risposta, o {@code null} se il piano è {@code null}
     */
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
                .build();
    }

    /**
     * Converte una lista di {@link Plan} in una lista di {@link PlanResponseDTO}.
     *
     * @param plans lista dei piani
     * @return lista dei DTO di risposta
     */
    public List<PlanResponseDTO> toResponseList(List<Plan> plans) {
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Costruisce un nuovo {@link Plan} a partire da un {@link PlanCreateRequestDTO}.
     *
     * @param request il DTO di creazione con i dati del piano
     * @return l'entità {@link Plan} pronta per il salvataggio
     */
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

    /**
     * Aggiorna i campi non {@code null} di un {@link Plan} esistente con i valori
     * presenti nel {@link PlanCreateRequestDTO}. I campi {@code null} o blank
     * nel DTO non sovrascrivono i valori correnti.
     *
     * @param request  il DTO con i nuovi valori (parziale)
     * @param existing il piano esistente da aggiornare in-place
     */
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
