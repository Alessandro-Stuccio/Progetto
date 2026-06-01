package com.project.tesi.repository;

import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Regole orarie ricorrenti dei professionisti, da cui lo scheduler genera gli slot della settimana.
 */
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    // Le fasce settimanali di un professionista, es. MONDAY 09:00-13:00, WEDNESDAY 14:00-18:00.
    List<WeeklySchedule> findByProfessional(User professional);

}