package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

@Data
public class AnalyticItem {

    private String sql;

    private AnalyticsType type = AnalyticsType.COUNT;

    private String message;
}
