package com.project.tesi.enums;

import javax.print.Doc;

/**
 * Tipologia di documento gestito dalla piattaforma.
 * Determina chi può caricare il documento e dove viene visualizzato.
 */
public enum DocumentType {
    INSURANCE_POLICE("polizza"),   // Polizza
    DIET_PLAN("dieta"),          // Piano nutrizionale
    WORKOUT_PLAN("scheda di allenamento"),       // Scheda allenamento
    MEDICAL_CERT("certificato medico");       // Certificato medico

    private String  desc;
    public String getDesc() {
        return desc;
    }

    DocumentType(String desc) {
        this.desc = desc;
    }


}