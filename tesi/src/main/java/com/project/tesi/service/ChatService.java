package com.project.tesi.service;

import com.project.tesi.dto.request.SendMessageRequest;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ChatService {

    Long createChat(@NotNull @Min(1) Long senderId, @NotNull @Min(1) Long receiverId);

    Message sendMessage(@NotNull @Valid SendMessageRequest request, @NotNull @Min(1) Long senderId);

    void sendMessageDirect(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long senderId,
                           @NotBlank String content);

    List<Message> getConversation(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId,
                                  @Min(0) int page, @Min(1) int size);

    List<Chat> getUserConversations(@NotNull @Min(1) Long userId);

    void markAsRead(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    int getTotalUnreadCount(@NotNull @Min(1) Long userId);

    String getUserFullName(@NotNull @Min(1) Long userId);

    Chat getChatEntity(@NotNull @Min(1) Long chatId);

    void closeChat(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long moderatorId);

    void closeChatByUser(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    Long getReceiverId(@NotNull Chat chat, @NotNull @Min(1) Long currentUserId);

    Message getLastMessage(@NotNull @Min(1) Long chatId);

    int getUnreadCount(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);
}
