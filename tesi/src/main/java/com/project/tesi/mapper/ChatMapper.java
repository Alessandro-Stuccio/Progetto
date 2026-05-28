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
 * Mapper per la conversione di {@link Message} e {@link Chat} in DTO di risposta.
 */
@Component
public class ChatMapper {

    /**
     * Converte un {@link Message} in {@link ChatMessageResponse}, risolvendo
     * mittente e destinatario a partire dall'entità {@link Chat} collegata.
     *
     * @param message il messaggio da convertire
     * @return il DTO di risposta con i dati del messaggio e degli interlocutori
     */
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

    /**
     * Converte una lista di {@link Message} in una lista di {@link ChatMessageResponse}.
     *
     * @param messages lista dei messaggi da convertire
     * @return lista dei DTO di risposta
     */
    public List<ChatMessageResponse> toMessageResponseList(List<Message> messages) {
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Costruisce un {@link ConversationPreviewResponse} per la lista delle
     * conversazioni, includendo il partner di conversazione, l'ultimo messaggio
     * e il conteggio dei messaggi non letti.
     *
     * @param chat          la chat da sintetizzare
     * @param currentUserId ID dell'utente corrente (determina chi è il partner)
     * @param lastMsg       ultimo messaggio della chat, o {@code null} se vuota
     * @param unreadCount   numero di messaggi non letti dall'utente corrente
     * @return il DTO di anteprima della conversazione
     */
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
