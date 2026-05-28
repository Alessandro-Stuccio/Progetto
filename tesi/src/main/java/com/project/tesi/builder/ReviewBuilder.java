package com.project.tesi.builder;

import java.time.LocalDateTime;
import com.project.tesi.model.*;


/**
 * Builder per l'entità Review. Fluent interface per la costruzione di una recensione.
 */
public interface ReviewBuilder {
    ReviewBuilder id(Long id);
    ReviewBuilder client(User client);
    ReviewBuilder professional(User professional);
    ReviewBuilder rating(int rating);
    ReviewBuilder comment(String comment);
    ReviewBuilder createdAt(LocalDateTime createdAt);
    Review build();
}
