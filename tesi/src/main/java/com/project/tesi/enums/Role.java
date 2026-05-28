package com.project.tesi.enums;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Ruoli disponibili nel sistema.
 * Determina i permessi e le funzionalità accessibili dall'utente.
 */
public enum Role {
    CLIENT,             // Utente finale
    PERSONAL_TRAINER,   // Professionista PT
    NUTRITIONIST,       // Professionista Nutrizionista
    MODERATOR,          // Moderatore operativo
    INSURANCE_MANAGER,  // Gestore polizze
    ADMIN;              // Amministratore sistema

    public static Set<Role> getManagebleRoles(Role role) {
        return switch (role) {
            case MODERATOR -> EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST);
            case INSURANCE_MANAGER -> EnumSet.of(Role.ADMIN);
            case ADMIN -> EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST, Role.MODERATOR, Role.INSURANCE_MANAGER);
            default -> new HashSet<>();
        };
    }

}