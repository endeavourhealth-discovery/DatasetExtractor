package org.endeavourhealth.reportgenerator.slack;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Slf4j
public class SlackReporter {


    public void report(List<Report> reports) {

        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        StringBuilder message = new StringBuilder();

        message.append("*Report Scheduler has run at " + today + "*");

        appendReportSummary( message, reports );

        appendFullReport( message, reports);

        log.info("Sending slack message");

        log.info(message.toString());
    }

    private void appendFullReport(StringBuilder message, List<Report> reports) {
        for (Report report : reports) {
            appendReport(message, report );
        }
    }

    private void appendReportSummary(StringBuilder message, List<Report> reports) {
        message.append("'''");
        for (Report report : reports) {
            appendReportSummary(message, report );
        }
        message.append("'''");

        breakLine( message );
    }

    private void appendReportSummary(StringBuilder message, Report report) {

        message.append("Report "+ report.getName() + " : " + report.getStatus());
    }

    private void breakLine(StringBuilder message) {
        message.append(System.lineSeparator());
    }

    private void appendReport(StringBuilder message, Report report) {

        message.append("Report "+ report.getName());
        message.append(report.toString());
        breakLine(message);


    }
}
