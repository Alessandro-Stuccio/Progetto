package com.project.tesi.builder;

import java.time.LocalDateTime;
import com.project.tesi.model.*;


/**
 * Costruisce una Review, cioè una recensione, con interfaccia fluente.
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
