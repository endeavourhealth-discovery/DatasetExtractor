package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

import java.time.DayOfWeek;

@Data
public class Schedule {

    private DayOfWeek dayOfWeek;

    private Boolean isDaily;

    private Boolean isMonthly;
}
