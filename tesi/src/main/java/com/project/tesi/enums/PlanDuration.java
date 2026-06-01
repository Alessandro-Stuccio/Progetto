package com.project.tesi.enums;

/**
 * Durata di un piano di abbonamento. A ogni valore è legato il numero di mesi
 * (getMonths), da cui si calcola la data di scadenza.
 */
public enum PlanDuration {
    SEMESTRALE(6),
    ANNUALE(12);

    private final int months;

    PlanDuration(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}