package com.project.tesi.enums;

/**
 * Tipologia di documento caricato sulla piattaforma. Ogni valore porta con sé
 * una descrizione leggibile (getDesc) usata nelle email e nel feed attività.
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
