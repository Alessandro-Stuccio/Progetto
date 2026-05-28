package com.project.tesi.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.tesi.model.*;


/**
 * Builder per l'entità Chat. Fluent interface per la costruzione della conversazione tra due utenti.
 */
public interface ChatBuilder {
    ChatBuilder id(Long id);
    ChatBuilder user1(User user1);
    ChatBuilder user2(User user2);
    ChatBuilder messages(List<Message> messages);
    ChatBuilder createdAt(LocalDateTime createdAt);
    Chat build();
}
