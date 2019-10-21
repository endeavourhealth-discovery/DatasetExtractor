package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

@Data
public class Analytics {

    private Boolean switchedOn = true;

    private String tableName;

    private AnalyticsType type = AnalyticsType.COUNT;

    private String friendlyName;

    private String message;
}
