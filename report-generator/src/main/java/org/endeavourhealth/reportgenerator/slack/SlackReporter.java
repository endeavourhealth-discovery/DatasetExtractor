package org.endeavourhealth.reportgenerator.slack;

import lombok.extern.slf4j.Slf4j;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.endeavourhealth.reportgenerator.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class SlackReporter {

    private final String slackAuditUrl;

    private final String slackErrorUrl;

    private boolean switchedOn  = true;

    private StringBuilder builder = new StringBuilder();

    public SlackReporter(String slackAuditUrl, String slackErrorUrl, String switchedOn) {
        super();
        this.slackAuditUrl = slackAuditUrl;
        this.slackErrorUrl = slackErrorUrl;
        if (switchedOn != null && switchedOn.equals("false")) this.switchedOn = false;
    }


    public void report(List<Report> reports) {

        if (!switchedOn) return;

        appendTitle();

        appendReportSummary(reports);

        appendPartialReport(reports);

        appendFullReport(reports);

        sendSlackAuditMessage(builder.toString());
    }

    private void appendPartialReport(List<Report> reports) {
        breakLine();
        append("*Partial Report Details*");
        append("```");

        for (Report report : reports) {

            append("*" + report.getName() + "*");

            append("Start time : " + report.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            append("End time : " + report.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if(report.getErrorMessage() != null) {
                append("ERROR! : " + report.getErrorMessage());
                breakLine();
                continue;
            }

            appendSchedule(report.getSchedule());

            if( report.isStfpSwitchedOn() ) {
                append("Sftp uploaded to : " + report.getSftpUpload().getUsername());
            } else {
                append("Sftp switched Off");
            }


            for (Table table : report.getCsvExport().getTables()) {
                builder.append("CSV Table : " + table.getFileName());
            }
            breakLine();

            appendAnalytics( report.getAnalytics() );

            append("Delta ran? : " + report.isDeltaReport());
            breakLine();
        }
        builder.append("```");
    }

    private void appendSchedule(Schedule schedule) {

        if(schedule == null) {
            append("No schedule configured, default run");
            return;
        }

        if(schedule.getIsDaily() != null) append("Is Daily? : " + schedule.getIsDaily());

        if(schedule.getSkipDays() != null) append("Skip days : " + schedule.getSkipDays());

        if(schedule.getDayOfWeek() != null) append("Day of week : " + schedule.getDayOfWeek());

        if(schedule.getDayOfMonth() != null) append("Day of month : " + schedule.getDayOfMonth());
    }

    private void appendAnalytics(Analytics analytics) {

        if(analytics == null) return;

        for (AnalyticItem item : analytics.getItems()) {
            append(item.getMessage());
        }
    }

    private void append(String message) {
        builder.append(message);
        breakLine();
    }

    private void appendTitle() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
        append("*Report Scheduler has run at " + today + "*");
    }

    private void sendSlackAuditMessage(String message) {

        log.info("Sending slack audit message");

        log.info(message);

        SlackMessage slackMessage = new SlackMessage(message);

        try {
            SlackApi slackApi = new SlackApi(slackAuditUrl);
            slackApi.call(slackMessage);
        } catch (Exception e) {
            log.error("Cannot send message to slack", e);
        }
    }

    public void sendSlackErrorMessage(String message) {

        log.info("Sending slack error message");

        log.info(message);

        SlackMessage slackMessage = new SlackMessage(message);

        try {
            SlackApi slackApi = new SlackApi(slackErrorUrl);
            slackApi.call(slackMessage);
        } catch (Exception e) {
            log.error("Cannot send message to slack", e);
        }
    }

    private void appendFullReport(List<Report> reports) {
        breakLine();
        builder.append("*Full Report Details*");
        builder.append("```");
        for (Report report : reports) {
            appendReport(report);
            breakLine(2);
        }
        builder.append("```");
    }

    private void breakLine(int loop) {
        for (int i = 0; i < loop; i++) {
            breakLine();
        }
    }

    private void appendReportSummary(List<Report> reports) {
        breakLine();
        builder.append("*Report Summary*");


        builder.append("```");
        for (Report report : reports) {
            appendReportSummary(report);
        }
        append("```");
    }

    private void appendReportSummary(Report report) {
        append("Report " + report.getName() + " : " + report.getStatus());
    }

    private void breakLine() {
        builder.append(System.lineSeparator());
    }

    private void appendReport(Report report) {
        builder.append("Report " + report.getName());
        append(report.toString());
    }
}
