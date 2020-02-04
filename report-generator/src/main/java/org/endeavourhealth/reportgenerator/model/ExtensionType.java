package org.endeavourhealth.reportgenerator.model;

import lombok.Getter;

public enum ExtensionType {

    DELTA("Delta"),
    DEANONYMISE_WF("Deanonymise Waltham Forest"),
    DEANONYMISE_EYE("Deanonymise Diabetes Eye Screen"),
    DEANONYMISE_FRAILTY("Deanonymise Frailty"),
    DEANONYMISE_ELGH("Deanonymise East London Genes & Health"),
    DEANONYMISE_WF_DIABETES("Deanonymise Waltham Forest Diabetes"),
    DEANONYMISE_BHR_DIABETES("Deanonymise BHR Diabetes");

    @Getter
    String displayName;

    ExtensionType(String displayName) {
        this.displayName = displayName;
    }
}
