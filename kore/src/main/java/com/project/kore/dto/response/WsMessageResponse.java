package com.project.kore.dto.response;

import java.util.Objects;

/**
 * Versione di un messaggio di chat pensata per l'invio in tempo reale via STOMP.
 * Date e stato sono già serializzati come stringhe per il client.
 */
public class WsMessageResponse {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private String status;
    private String createdAt;
    private String roomId;

    private WsMessageResponse(Long id, Long chatId, Long senderId, String senderName,
                               Long receiverId, String receiverName, String content,
                               String status, String createdAt, String roomId) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.roomId = roomId;
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public Long getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getRoomId() { return roomId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WsMessageResponse that = (WsMessageResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WsMessageResponse{id=" + id + ", chatId=" + chatId + ", senderId=" + senderId + ", status='" + status + "'}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long chatId;
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String receiverName;
        private String content;
        private String status;
        private String createdAt;
        private String roomId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder chatId(Long chatId) { this.chatId = chatId; return this; }
        public Builder senderId(Long senderId) { this.senderId = senderId; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder receiverId(Long receiverId) { this.receiverId = receiverId; return this; }
        public Builder receiverName(String receiverName) { this.receiverName = receiverName; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(String createdAt) { this.createdAt = createdAt; return this; }
        public Builder roomId(String roomId) { this.roomId = roomId; return this; }

        public WsMessageResponse build() {
            return new WsMessageResponse(id, chatId, senderId, senderName, receiverId,
                    receiverName, content, status, createdAt, roomId);
        }
    }
}
