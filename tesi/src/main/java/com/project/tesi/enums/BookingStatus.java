package com.project.tesi.enums;

/**
 * Stato di una prenotazione nel suo ciclo di vita.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELED,
    COMPLETED   // lezione avvenuta: è la condizione che abilita le recensioni
}