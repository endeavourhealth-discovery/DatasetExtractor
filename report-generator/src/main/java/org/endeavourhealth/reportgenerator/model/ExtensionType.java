package org.endeavourhealth.reportgenerator.model;

import lombok.Getter;

public enum ExtensionType {

    DELTA("Delta"),
    DEANONYMISE_WF("Deanonymise Waltham Forest"),
    DEANONYMISE_EYE("Deanonymise Diabetes Eye Screen"),
    DEANONYMISE_ELGH("Deanonymise East London Genes & Health") ;

    @Getter
    String displayName;

    ExtensionType(String displayName) {
        this.displayName = displayName;
    }
}
