package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface MessageService {

    Message saveMessage(@NotNull Chat chat, @NotNull User sender, @NotBlank String content);

    List<Message> getMessages(@NotNull @Min(1) Long chatId, @Min(0) int page, @Min(1) int size);

    void markAsDelivered(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    void markAsRead(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    int getTotalUnreadCount(@NotNull @Min(1) Long userId);

    Message getLastMessage(@NotNull @Min(1) Long chatId);

    int getUnreadCount(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);
}
