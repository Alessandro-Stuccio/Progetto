package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Servizio per la gestione dei calendari settimanali dei professionisti.
 */
@Validated
public interface WeeklyScheduleService {

    /**
     * Restituisce la lista degli schedule settimanali associati a un professionista.
     *
     * @param professional utente professionista di cui recuperare gli schedule
     * @return lista dei calendari settimanali del professionista
     */
    List<WeeklySchedule> findByProfessional(@NotNull User professional);
}
