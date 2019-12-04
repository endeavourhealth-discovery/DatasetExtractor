package org.endeavourhealth.reportgenerator.model;

public enum ReportStatus {
    SUCCESS("Success"), INACTIVE("Inactive"), FAILURE("Failure"), NOT_SCHEDULED("Not scheduled"), INVALID_CONFIGURATION("Invalid Configuration");

    private final String displayName;

    ReportStatus(String displayName) {

        this.displayName = displayName;
    }

    String getDisplayName() {
        return displayName;
    }
}
