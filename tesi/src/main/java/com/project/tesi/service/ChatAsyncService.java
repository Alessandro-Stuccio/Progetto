package com.project.tesi.service;

public interface ChatAsyncService {

    void saveChatMessage(Long chatId, Long senderId, String content);

    void markAsDeliveredAsync(Long chatId, Long userId);

    void markAsReadAsync(Long chatId, Long userId);
}
