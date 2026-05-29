package com.project.tesi.security;

import com.project.tesi.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) {
        try {
            http
                    .cors(cors -> cors.configurationSource(request -> {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(allowedOrigins);
                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        config.setAllowedHeaders(List.of("*"));
                        config.setAllowCredentials(true);
                        return config;
                    }))
                    .csrf(AbstractHttpConfigurer::disable)
                    .headers(headers -> headers
                            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                            .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/api/plans/**").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/professionals/*/slots").hasAnyRole(Role.CLIENT.name(), Role.PERSONAL_TRAINER.name(), Role.NUTRITIONIST.name())
                            .requestMatchers("/api/professionals/**").permitAll()
                            .requestMatchers("/api/reviews/professional/**").permitAll()
                            .requestMatchers("/api/job-applications/**").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/chat/*/close").hasAnyRole(Role.MODERATOR.name(), Role.ADMIN.name())
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews").hasRole(Role.CLIENT.name())
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/subscriptions/activate").hasRole(Role.CLIENT.name())
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/dashboard").hasRole(Role.CLIENT.name())
                            .requestMatchers("/api/bookings/**").hasRole(Role.CLIENT.name())
                            .requestMatchers( "/api/admin/**").hasRole(Role.ADMIN.name())
                            .requestMatchers("/api/moderator/**").hasAnyRole(Role.MODERATOR.name(),Role.ADMIN.name())
                            .requestMatchers("/api/insurance/**").hasRole(Role.INSURANCE_MANAGER.name())
                            .anyRequest().authenticated())
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
