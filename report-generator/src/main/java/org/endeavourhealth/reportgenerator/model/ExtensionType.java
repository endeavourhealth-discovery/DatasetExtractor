package org.endeavourhealth.reportgenerator.model;

import lombok.Getter;

public enum ExtensionType {

    DELTA("Delta"), DEANONYMISE_WF("Deanonymise");

    @Getter
    String displayName;

    ExtensionType(String displayName) {
        this.displayName = displayName;
    }
}
