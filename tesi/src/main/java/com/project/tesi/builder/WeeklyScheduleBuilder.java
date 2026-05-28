package com.project.tesi.builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import com.project.tesi.model.*;


/**
 * Builder per l'entità WeeklySchedule. Fluent interface per la costruzione di una fascia oraria settimanale di un professionista.
 */
public interface WeeklyScheduleBuilder {
    WeeklyScheduleBuilder id(Long id);
    WeeklyScheduleBuilder professional(User professional);
    WeeklyScheduleBuilder dayOfWeek(DayOfWeek dayOfWeek);
    WeeklyScheduleBuilder startTime(LocalTime startTime);
    WeeklyScheduleBuilder endTime(LocalTime endTime);
    WeeklySchedule build();
}
