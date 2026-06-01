package com.project.tesi.scheduler;

import com.project.tesi.enums.Role;
import com.project.tesi.facade.ProfessionalFacade;
import com.project.tesi.model.User;
import com.project.tesi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera gli slot della settimana successiva per ogni personal trainer e
 * nutrizionista, ogni domenica a mezzanotte.
 */
@Component
public class SlotGenerationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlotGenerationScheduler.class);

    private final ProfessionalFacade professionalFacade;
    private final UserService userService;

    public SlotGenerationScheduler(ProfessionalFacade professionalFacade, UserService userService) {
        this.professionalFacade = professionalFacade;
        this.userService = userService;
    }

    // Ogni domenica a mezzanotte: copre la settimana che parte da oggi + 7 giorni
    // e genera gli slot per ogni professionista.
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void generateWeeklySlotsForAllProfessionals() {
        List<User> professionals = new ArrayList<>(userService.findByRole(Role.PERSONAL_TRAINER));
        professionals.addAll(userService.findByRole(Role.NUTRITIONIST));
        LocalDate start = LocalDate.now().plusDays(7);
        LocalDate end = start.plusDays(6);
        for (User pro : professionals) {
            try {
                professionalFacade.generateSlotsFromSchedule(pro, start, end);
            } catch (Exception e) {
                log.error("Errore generazione slot per professionista {}: {}", pro.getId(), e.getMessage());
            }
        }
    }
}
