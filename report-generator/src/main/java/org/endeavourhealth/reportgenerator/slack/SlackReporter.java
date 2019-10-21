package org.endeavourhealth.reportgenerator.slack;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class SlackReporter {

    private StringBuilder builder = new StringBuilder();

    public void report(List<Report> reports) {

        appendTitle();

        appendReportSummary(reports);

        appendPartialReport(reports);

        appendFullReport(reports);

        sendSlackMessage(builder.toString());
    }

    private void appendPartialReport(List<Report> reports) {
        breakLine();
        builder.append("*Partial Report Details*");
        builder.append("```");

        for (Report report : reports) {

            builder.append(report.getName());
            breakLine();

            if(report.getErrorMessage() != null) {
                builder.append("ERROR! : " + report.getErrorMessage());
                breakLine();
                continue;
            }

            builder.append("Sftp Switched On : " + report.getSftpUpload().getSwitchedOn());
            breakLine();

            for (Table table : report.getCsvExport().getTables()) {
                builder.append("CSV Table : " + table.getName() + " with filename " + table.getFileName());
            }
            breakLine();

            builder.append("Delta ran? : " + report.isDelta());
            breakLine();
        }
        builder.append("```");
    }

    private void appendTitle() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss"));
        builder.append("*Report Scheduler has run at " + today + "*");
        breakLine();
    }

    private void sendSlackMessage(String message) {

        log.info("Sending slack builder");

        log.info(message);

//        SlackHelper.sendSlackMessage(SlackHelper.Channel.ReportSchedulerAlerts, message);
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
        builder.append("```");

        breakLine();
    }

    private void appendReportSummary(Report report) {

        builder.append("Report " + report.getName() + " : " + report.getStatus());
        breakLine();
    }

    private void breakLine() {
        builder.append(System.lineSeparator());
    }

    private void appendReport(Report report) {
        builder.append("Report " + report.getName());
        builder.append(report.toString());
        breakLine();
    }
}
