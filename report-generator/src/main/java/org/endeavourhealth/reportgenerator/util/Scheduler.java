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


    public boolean isScheduled(Schedule schedule) {

        if(schedule == null) {
            log.warn("No scheduler configured, default behaviour is to run report");
            return true;
        }

        log.info("Running scheduler for time {}", now);

        log.info(schedule.toString());

        if(schedule.getIsDaily() != null) {
            return checkDaily( schedule );
        }

        if(schedule.getDayOfWeek() != null) {
            return checkWeekly( schedule );
        }

        if(schedule.getDayOfMonth() != null) {
            return checkMonthly( schedule );
        }

        //Should never get here
        throw new ReportGeneratorException("Scheduler config is messed up, fix the validator! " + schedule.toString());
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

        if(schedule.getSkipDays() != null && schedule.getSkipDays().contains( dayOfWeek ))
            return false;

        return true;
    }
}
