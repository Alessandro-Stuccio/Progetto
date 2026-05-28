package com.project.tesi.enums;

/**
 * Stato di una conversazione chat.
 * {@code OPEN}: chat attiva; {@code CLOSED}: chat chiusa dal moderatore
 * (riapribile dal cliente inviando un nuovo messaggio).
 */
public enum ChatStatus {
    OPEN,
    CLOSED
}
