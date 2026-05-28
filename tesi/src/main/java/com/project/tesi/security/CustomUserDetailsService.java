package com.project.tesi.security;

import com.project.tesi.model.User;
import com.project.tesi.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Configurazione del {@link UserDetailsService} di Spring Security.
 * Carica l'utente dall'email escludendo gli account soft-deleted.
 */
@Configuration
public class CustomUserDetailsService{

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Espone un bean {@link UserDetailsService} che cerca l'utente per email
     * con il flag {@code deleted=false}. Lancia {@link org.springframework.security.core.userdetails.UsernameNotFoundException}
     * se l'utente non esiste o è stato eliminato.
     *
     * @return il {@link UserDetailsService} configurato
     */
    @Bean
    public UserDetailsService getUserDetails(){
        return email -> userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));
    }
}
