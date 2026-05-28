package com.project.tesi.enums;

/**
 * Tipologia di documento caricato sulla piattaforma. Ogni valore espone
 * una descrizione leggibile ({@link #getDesc()}) usata nei template email
 * e nel feed attività.
 */
public enum DocumentType {
    INSURANCE_POLICE("polizza"),
    DIET_PLAN("dieta"),
    WORKOUT_PLAN("scheda di allenamento");

    private final String desc;

    public String getDesc() {
        return desc;
    }

    DocumentType(String desc) {
        this.desc = desc;
    }
}
