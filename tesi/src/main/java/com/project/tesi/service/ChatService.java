package com.project.tesi.service;

import com.project.tesi.model.Chat;
import com.project.tesi.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ChatService {

    Long getOrCreateChat(@NotNull User sender, @NotNull User receiver);

    List<Chat> getUserConversations(@NotNull @Min(1) Long userId);

    Chat getChatEntity(@NotNull @Min(1) Long chatId);

    Chat save(@NotNull Chat chat);

    long countOpenChatsByModerator(@NotNull @Min(1) Long moderatorId);

    void closeChat(@NotNull @Min(1) Long chatId, @NotNull User moderator);
}
