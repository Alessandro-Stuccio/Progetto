package com.project.tesi.facade;

import com.project.tesi.dto.response.ActivityFeedItemResponse;

import java.util.List;

public interface ActivityFeedFacade {
    List<ActivityFeedItemResponse> getActivityFeed(Long userId, int days, int limit);
    void logDocumentUploaded(Long clientId, Long uploaderId, String type);
}
