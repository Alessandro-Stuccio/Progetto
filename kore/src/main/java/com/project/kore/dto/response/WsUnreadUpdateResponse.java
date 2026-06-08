package com.project.kore.dto.response;

/**
 * Notifica WebSocket che aggiorna il badge dei messaggi non letti di un utente (type UNREAD_UPDATE).
 */
public class WsUnreadUpdateResponse {

    private String type;
    private Long userId;
    private int unreadCount;


    private WsUnreadUpdateResponse(Builder b) {
        this.type = b.type;
        this.userId = b.userId;
        this.unreadCount = b.unreadCount;
    }

    public static Builder builder() { return new Builder(); }

    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public int getUnreadCount() { return unreadCount; }

    public static class Builder {
        private String type;
        private Long userId;
        private int unreadCount;

        public Builder type(String type) { this.type = type; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder unreadCount(int unreadCount) { this.unreadCount = unreadCount; return this; }

        public WsUnreadUpdateResponse build() { return new WsUnreadUpdateResponse(this); }
    }
}
