package com.project.tesi.builder;

import com.project.tesi.enums.MessageStatus;
import com.project.tesi.model.Chat;
import com.project.tesi.model.Message;

import java.time.LocalDateTime;

/**
 * Costruisce un Message di chat con interfaccia fluente.
 */
public interface MessageBuilder {
    MessageBuilder id(Long id);
    MessageBuilder content(String content);
    MessageBuilder timeStamp(LocalDateTime timeStamp);
    MessageBuilder status(MessageStatus status);
    MessageBuilder sentByUser1(boolean sentByUser1);
    MessageBuilder chat(Chat chat);
    Message build();
}
