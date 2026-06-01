package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.facade.ActivityFeedFacade;
import com.project.tesi.mapper.ActivityFeedMapper;
import com.project.tesi.model.Document;
import com.project.tesi.model.Slot;
import com.project.tesi.model.User;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.SlotService;
import com.project.tesi.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Costruisce il feed attività aggregando gli eventi recenti dell'utente: prenotazioni e documenti
 * degli ultimi N giorni, con sorgenti diverse a seconda del ruolo.
 */
@Component
public class ActivityFeedFacadeImpl implements ActivityFeedFacade {

    private final UserService userService;
    private final SlotService slotService;
    private final DocumentService documentService;
    private final ActivityFeedMapper activityFeedMapper;

    public ActivityFeedFacadeImpl(UserService userService,
                                   SlotService slotService,
                                   DocumentService documentService,
                                   ActivityFeedMapper mapper) {
        this.userService = userService;
        this.slotService = slotService;
        this.documentService = documentService;
        this.activityFeedMapper = mapper;
    }

    // Un cliente vede i propri slot e documenti; un professionista quelli legati ai suoi clienti.
    // Gli altri ruoli non hanno feed e restano con liste vuote.
    @Override
    @Transactional(readOnly = true)
    public List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit) {
        User user = userService.getUserById(userId);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Slot> slots = new ArrayList<>();
        List<Document> documents = new ArrayList<>();

        if (user.getRole() == Role.CLIENT) {
            slots = slotService.findRecentByUser(user, since);
            documents = documentService.findRecentByOwner(user, since);
        } else if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            slots = slotService.findRecentByProfessional(user, since);
            documents = documentService.findRecentByProfessional(user, since);
        }

        return activityFeedMapper.toActivityFeedItemResponse(slots, documents, user);
    }

}
