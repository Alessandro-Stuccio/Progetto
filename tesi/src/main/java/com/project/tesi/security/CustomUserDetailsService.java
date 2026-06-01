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
 * Fornisce a Spring Security lo {@link UserDetailsService}: carica l'utente
 * dall'email, ignorando gli account soft-deleted.
 */
@Configuration
public class CustomUserDetailsService{

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cerca per email tra gli utenti non eliminati; se non lo trova solleva
    // UsernameNotFoundException.
    @Bean
    public UserDetailsService getUserDetails(){
        return email -> userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));
    }
}
