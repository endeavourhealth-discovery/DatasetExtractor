package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.exception.ReportGeneratorException;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Slf4j
public class Scheduler {

    private LocalDateTime now = LocalDateTime.now();


    public boolean isScheduled(Report report) {

        Schedule schedule = report.getSchedule();

        if(schedule.getIsDaily()) {
            return checkDaily( schedule );
        }

        if(schedule.getDayOfWeek() != null) {
            return checkWeekly(schedule);
        }

        if(schedule.getDayOfMonth() != null) {
            return checkMonthly(schedule);
        }

        //Should never get here
        throw new ReportGeneratorException("Scheduler config is messed up, fix the validator! " + report.toString());
    }

    private boolean checkWeekly(Schedule schedule) {
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        if(schedule.getDayOfWeek() == dayOfWeek) return true;

        return false;
    }

    private boolean checkMonthly(Schedule schedule) {
        if( schedule.getDayOfMonth().equals(now.getDayOfMonth()) ) return true;

        return false;
    }

    private boolean checkDaily(Schedule schedule) {
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        if(schedule.getSkipDays().contains( dayOfWeek ))
            return false;

        return true;
    }
}
