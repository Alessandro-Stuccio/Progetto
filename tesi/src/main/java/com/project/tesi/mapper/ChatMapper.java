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

/**
 * Converte messaggi e chat nei DTO usati dalla messaggistica.
 */
@Component
public class ChatMapper {

    // Mittente e destinatario si ricavano dal flag sentByUser1 sulla Chat collegata.
    public ChatMessageResponse toMessageResponse(Message message) {
        User sender = message.isSentByUser1()
                ? message.getChat().getUser1()
                : message.getChat().getUser2();
        User receiver = message.isSentByUser1()
                ? message.getChat().getUser2()
                : message.getChat().getUser1();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiver.getId())
                .receiverName(receiver.getFullName())
                .content(message.getContent())
                .createdAt(message.getTimeStamp())
                .status(message.getStatus())
                .build();
    }

    public List<ChatMessageResponse> toMessageResponseList(List<Message> messages) {
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    // Anteprima per la lista chat: il partner è l'altro utente rispetto a currentUserId.
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
