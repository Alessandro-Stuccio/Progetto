package com.project.kore.dto.response;

import com.project.kore.enums.MessageStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Un messaggio di chat restituito via REST, con il relativo stato di lettura (SENT, DELIVERED, READ).
 */
public class ChatMessageResponse {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private MessageStatus status;
    private LocalDateTime createdAt;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessageResponse that = (ChatMessageResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChatMessageResponse{id=" + id + ", chatId=" + chatId + ", senderId=" + senderId + ", status=" + status + "}";
    }

    public static class Builder {
        private Long id;
        private Long chatId;
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String receiverName;
        private String content;
        private MessageStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder chatId(Long chatId) { this.chatId = chatId; return this; }
        public Builder senderId(Long senderId) { this.senderId = senderId; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder receiverId(Long receiverId) { this.receiverId = receiverId; return this; }
        public Builder receiverName(String receiverName) { this.receiverName = receiverName; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder status(MessageStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChatMessageResponse build() {
            ChatMessageResponse obj = new ChatMessageResponse();
            obj.id = this.id;
            obj.chatId = this.chatId;
            obj.senderId = this.senderId;
            obj.senderName = this.senderName;
            obj.receiverId = this.receiverId;
            obj.receiverName = this.receiverName;
            obj.content = this.content;
            obj.status = this.status;
            obj.createdAt = this.createdAt;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
