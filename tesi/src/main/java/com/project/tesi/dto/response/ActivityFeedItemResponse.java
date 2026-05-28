package com.project.tesi.dto.response;

import java.time.LocalDateTime;

public class ActivityFeedItemResponse {

    private String type;
    private String text;
    private LocalDateTime timestamp;


    private ActivityFeedItemResponse(Builder b) {
        this.type = b.type;
        this.text = b.text;
        this.timestamp = b.timestamp;
    }

    public static Builder builder() { return new Builder(); }

    public String getType() { return type; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public static class Builder {
        private String type;
        private String text;
        private LocalDateTime timestamp;

        public Builder type(String type) { this.type = type; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ActivityFeedItemResponse build() { return new ActivityFeedItemResponse(this); }
    }
}
