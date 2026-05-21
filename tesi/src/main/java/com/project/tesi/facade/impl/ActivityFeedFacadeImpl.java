package com.project.tesi.facade.impl;

import com.project.tesi.dto.response.ActivityFeedItemResponse;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceNotFoundException;
import com.project.tesi.facade.IActivityFeedFacade;
import com.project.tesi.mapper.ActivityFeedMapper;
import com.project.tesi.model.Booking;
import com.project.tesi.model.Document;
import com.project.tesi.model.User;
import com.project.tesi.service.ActivityFeedService;
import com.project.tesi.service.BookingService;
import com.project.tesi.service.DocumentService;
import com.project.tesi.service.UserService;
import com.project.tesi.service.impl.ActivityFeedServiceImpl;
import com.project.tesi.service.impl.BookingServiceImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActivityFeedFacadeImpl implements IActivityFeedFacade {

    private final ActivityFeedService activityFeedService;
    private final UserService  userService;
    private final BookingService bookingService;
    private final DocumentService documentService;

    private final ActivityFeedMapper activityFeedMapper;

    public ActivityFeedFacadeImpl(ActivityFeedService activityFeedService, UserService userService, BookingService  bookingService, DocumentService documentService, ActivityFeedMapper mapper) {
        this.activityFeedService = activityFeedService;
        this.userService = userService;
        this.bookingService=bookingService;
        this.documentService = documentService;
        this.activityFeedMapper=mapper;
    }

    @Override
    public List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit) {
        User user=userService.getUserById(userId);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Booking> sortable = new ArrayList<>();
        List<Document> documents=new ArrayList<>();

        if (user.getRole() == Role.CLIENT) {
            sortable=bookingService.findRecentByUser(user,since);
            documents=documentService.findRecentByOwner(user,since);
        } else if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            sortable=bookingService.findRecentByProfessional(user,since);
            documents=documentService.findRecentByProfessional(user,since);
        }

        return activityFeedMapper.toActivityFeedItemResponse(sortable,documents,user);
    }

    @Override
    public void logDocumentUploaded(Long clientId, Long uploaderId, String type) {
        activityFeedService.logDocumentUploaded(clientId, uploaderId, type);
    }
}
