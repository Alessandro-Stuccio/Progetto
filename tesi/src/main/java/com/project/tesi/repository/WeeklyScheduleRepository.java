package com.project.tesi.repository;

import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository per l'accesso ai dati dell'entità {@link WeeklySchedule}.
 *
 * Permette di recuperare le regole orarie ricorrenti di un professionista,
 * usate dallo scheduler automatico per generare gli {@link com.project.tesi.model.Slot}
 * della settimana successiva.
 */
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    /**
     * Restituisce tutte le fasce orarie settimanali di un professionista.
     * Esempio: un PT potrebbe avere regole MONDAY 09:00–13:00, WEDNESDAY 14:00–18:00.
     *
     * @param professional il professionista
     * @return lista delle fasce orarie ricorrenti
     */
    List<WeeklySchedule> findByProfessional(User professional);

}