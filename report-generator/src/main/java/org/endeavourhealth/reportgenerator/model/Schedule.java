package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class Schedule {

    private DayOfWeek dayOfWeek;

    private Boolean isDaily;

    private Integer dayOfMonth;

    private List<DayOfWeek> skipDays;
}
