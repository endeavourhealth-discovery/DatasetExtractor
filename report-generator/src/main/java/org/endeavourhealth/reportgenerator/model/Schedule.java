package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.time.DayOfWeek;
import java.util.List;

@Data
@Entity
public class Schedule extends AbstractEntity{

    private DayOfWeek dayOfWeek;

    private Boolean isDaily;

    private Integer dayOfMonth;

    @ElementCollection
    private List<DayOfWeek> skipDays;
}
