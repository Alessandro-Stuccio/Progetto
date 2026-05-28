package com.project.tesi.util;

/**
 * Costanti di business condivise tra i layer applicativi.
 * {@code MAX_CLIENTS_PER_PROFESSIONAL=50}, {@code MAX_MESSAGE_LENGTH=2000},
 * {@code MIN_PASSWORD_LENGTH=8}.
 */
public final class BusinessConstants {

    private BusinessConstants() {}

    public static final int MAX_CLIENTS_PER_PROFESSIONAL = 50;
    public static final int MAX_MESSAGE_LENGTH = 2000;
    public static final int MIN_PASSWORD_LENGTH = 8;
}
