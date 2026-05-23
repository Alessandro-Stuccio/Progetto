package com.project.tesi.mapper;

import com.project.tesi.dto.response.ChatMessageResponse;
import com.project.tesi.dto.response.ConversationPreviewResponse;
import com.project.tesi.enums.MessageStatus;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;
import com.project.tesi.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMapper {

    public ChatMessageResponse toMessageResponse(Message message, Long receiverId) {
        User sender = message.isSentByUser1()
                ? message.getChat().getUser1()
                : message.getChat().getUser2();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiverId)
                .content(message.getContent())
                .createdAt(message.getTimeStamp())
                .status(message.isRead() ? MessageStatus.READ : MessageStatus.SENT)
                .build();
    }

    public List<ChatMessageResponse> toMessageResponseList(List<Message> messages, Long receiverId) {
        return messages.stream()
                .map(m -> toMessageResponse(m, receiverId))
                .collect(Collectors.toList());
    }

    public ConversationPreviewResponse toConversationPreview(Chat chat, Long currentUserId,
                                                              Message lastMsg, int unreadCount) {
        User partner = chat.getUser1().getId().equals(currentUserId)
                ? chat.getUser2() : chat.getUser1();
        return ConversationPreviewResponse.builder()
                .chatId(chat.getId())
                .otherUserId(partner.getId())
                .otherUserName(partner.getFullName())
                .otherUserRole(partner.getRole() != null ? partner.getRole().name() : null)
                .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                .lastMessageTime(lastMsg != null ? lastMsg.getTimeStamp() : null)
                .unreadCount(unreadCount)
                .terminated(chat.getStatus() == com.project.tesi.enums.ChatStatus.CLOSED)
                .build();
    }
}
