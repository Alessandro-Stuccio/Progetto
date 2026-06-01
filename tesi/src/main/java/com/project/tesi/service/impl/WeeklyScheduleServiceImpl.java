package com.project.tesi.service.impl;

import com.project.tesi.model.User;
import com.project.tesi.model.WeeklySchedule;
import com.project.tesi.repository.WeeklyScheduleRepository;
import com.project.tesi.service.WeeklyScheduleService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Recupero dei calendari settimanali dei professionisti. */
@Service
public class WeeklyScheduleServiceImpl implements WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public WeeklyScheduleServiceImpl(WeeklyScheduleRepository weeklyScheduleRepository) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    @Override
    public List<WeeklySchedule> findByProfessional(User professional) {
        return weeklyScheduleRepository.findByProfessional(professional);
    }

}
