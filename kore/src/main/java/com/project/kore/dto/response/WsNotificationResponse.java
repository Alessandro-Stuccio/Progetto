package com.project.kore.dto.response;

/**
 * Notifica WebSocket privata che avvolge un messaggio; type ne indica la natura
 * (NEW_MESSAGE, DELIVERED_UPDATE, READ_UPDATE).
 */
public class WsNotificationResponse {

    private String type;
    private WsMessageResponse message;


    private WsNotificationResponse(Builder b) {
        this.type = b.type;
        this.message = b.message;
    }

    public static Builder builder() { return new Builder(); }

    public String getType() { return type; }
    public WsMessageResponse getMessage() { return message; }

    public static class Builder {
        private String type;
        private WsMessageResponse message;

        public Builder type(String type) { this.type = type; return this; }
        public Builder message(WsMessageResponse message) { this.message = message; return this; }

        public WsNotificationResponse build() { return new WsNotificationResponse(this); }
    }
}
