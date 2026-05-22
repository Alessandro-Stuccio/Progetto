package com.project.tesi.enums;

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
