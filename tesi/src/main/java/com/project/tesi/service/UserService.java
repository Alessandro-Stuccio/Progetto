package com.project.tesi.service;

import com.project.tesi.model.User;
import com.project.tesi.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/** Gestione degli utenti della piattaforma. */
@Validated
public interface UserService {

    User getUserById(
            @NotNull(message = "l'id deve essere valorizzato")
            @Min(value = 1, message = "non esistono id negativi") Long id);

    /** Cerca l'utente per email, che qui fa anche da username. */
    User getUserByEmail(@NotNull String email);

    boolean existsByEmail(@NotNull String email);

    User save(@NotNull User user);

    List<User> findByRole(@NotNull Role role);

    List<User> findAll();

    long countByAssignedPT(@NotNull User pt);

    long countByAssignedNutritionist(@NotNull User nutritionist);

    List<User> findByAssignedPT(@NotNull User pt);

    List<User> findByAssignedNutritionist(@NotNull User nutritionist);

    /** Email già in uso da un altro utente, escludendo se stessi: serve in fase di update profilo. */
    boolean existsUserByEmailExcluding(String email, Long excludeId);

    void deleteUser(Long id);

    /** Applica l'hashing configurato a una password in chiaro. */
    String encodePassword(@NotNull String rawPassword);
}
