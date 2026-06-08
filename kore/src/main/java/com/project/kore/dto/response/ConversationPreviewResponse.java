package com.project.kore.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Anteprima di una conversazione nella lista chat: interlocutore, ultimo messaggio e numero di non letti.
 */
public class ConversationPreviewResponse {

    private Long chatId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserRole;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private boolean terminated;


    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserRole() { return otherUserRole; }
    public void setOtherUserRole(String otherUserRole) { this.otherUserRole = otherUserRole; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isTerminated() { return terminated; }
    public void setTerminated(boolean terminated) { this.terminated = terminated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationPreviewResponse that = (ConversationPreviewResponse) o;
        return Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "ConversationPreviewResponse{chatId=" + chatId + ", otherUserId=" + otherUserId + ", unreadCount=" + unreadCount + "}";
    }

    public static class Builder {
        private Long chatId;
        private Long otherUserId;
        private String otherUserName;
        private String otherUserRole;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private int unreadCount;
        private boolean terminated;

        public Builder chatId(Long chatId) { this.chatId = chatId; return this; }
        public Builder otherUserId(Long otherUserId) { this.otherUserId = otherUserId; return this; }
        public Builder otherUserName(String otherUserName) { this.otherUserName = otherUserName; return this; }
        public Builder otherUserRole(String otherUserRole) { this.otherUserRole = otherUserRole; return this; }
        public Builder lastMessage(String lastMessage) { this.lastMessage = lastMessage; return this; }
        public Builder lastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; return this; }
        public Builder unreadCount(int unreadCount) { this.unreadCount = unreadCount; return this; }
        public Builder terminated(boolean terminated) { this.terminated = terminated; return this; }

        public ConversationPreviewResponse build() {
            ConversationPreviewResponse obj = new ConversationPreviewResponse();
            obj.chatId = this.chatId;
            obj.otherUserId = this.otherUserId;
            obj.otherUserName = this.otherUserName;
            obj.otherUserRole = this.otherUserRole;
            obj.lastMessage = this.lastMessage;
            obj.lastMessageTime = this.lastMessageTime;
            obj.unreadCount = this.unreadCount;
            obj.terminated = this.terminated;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
