package com.project.tesi.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.tesi.model.Plan;
import java.util.List;

/**
 * Servizio per la gestione dei piani di abbonamento disponibili.
 */
@Validated
public interface PlanService {

    /**
     * Restituisce la lista di tutti i piani di abbonamento presenti nel sistema.
     *
     * @return lista di tutti i piani
     */
    List<Plan> getAllPlans();

    /**
     * Recupera un piano tramite il suo identificativo.
     *
     * @param id identificativo del piano
     * @return piano corrispondente
     */
    Plan getPlanById(@NotNull @Min(1) Long id);

    /**
     * Crea e persiste un nuovo piano di abbonamento.
     *
     * @param plan entità piano da creare
     * @return piano salvato
     */
    Plan createPlan(@NotNull Plan plan);

    /**
     * Elimina un piano di abbonamento tramite il suo identificativo.
     *
     * @param id identificativo del piano da eliminare
     */
    void deletePlan(@NotNull @Min(1) Long id);

    /**
     * Verifica se esiste già un piano con il nome specificato.
     *
     * @param name nome del piano da cercare
     * @return {@code true} se un piano con quel nome esiste, {@code false} altrimenti
     */
    boolean existsByName(@NotNull String name);
}
