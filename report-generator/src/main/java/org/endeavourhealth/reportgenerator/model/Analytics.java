package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

import java.util.List;

@Data
public class Analytics {

    private Boolean switchedOn = true;

    private List<AnalyticItem> items;
}
