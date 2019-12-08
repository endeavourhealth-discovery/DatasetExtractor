package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.ReportStatus;
import org.endeavourhealth.reportgenerator.repository.ReportRepository;
import org.endeavourhealth.reportgenerator.slack.SlackReporter;
import org.endeavourhealth.reportgenerator.validator.ReportValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportRunner {

    public static void main(String... args) throws IOException {

        Properties properties = loadProperties( args );

        List<Report> reports = loadReports( properties );

        SlackReporter slackReporter = new SlackReporter( properties);

        ReportRepository reportRepository = new ReportRepository( properties );

        try (  ReportGenerator reportGenerator = new ReportGenerator( properties ) ) {

            reportGenerator.generate( reports );

            reportRepository.save( reports );

            log.info("Report generation all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
            slackReporter.sendSlackErrorMessage("There has been an unexpected error in the report generator, please see logs for further details " + e.getMessage());
        }

        slackReporter.report( reports );

        if(reports.stream().anyMatch(r -> r.getStatus() == ReportStatus.FAILURE) == true) {
            slackReporter.sendSlackErrorMessage("There has been a report failure, please see aws-extract-reports channel for further details");
        };
    }

    private static Properties loadProperties(String[] args) throws IOException {

        Properties properties = new Properties();

        InputStream inputStream = ReportRunner.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load( inputStream );

        if(args.length > 0) {
            String reportYamlFile = args[0];
            if(!reportYamlFile.contains(".")) {
                //shorthand
                reportYamlFile = "report." + reportYamlFile + ".yaml";
            }
            properties.put("report.yaml.file", reportYamlFile);
        }

        if(args.length > 1) {
            String reportYamlDirectory = args[1];
            properties.put("report.yaml.directory", reportYamlDirectory);
        }

        log.debug("Using report.yaml.file {}", properties.get("report.yaml.file"));
        log.debug("Using report.yaml.directory {}", properties.get("report.yaml.directory"));

        return properties;
    }

    private static List<Report> loadReports(Properties properties) throws FileNotFoundException {

        List<Report> reports = new ArrayList<>();

        ReportValidator reportValidator = new ReportValidator();

        Yaml yaml = new Yaml(new Constructor(Report.class));

        String reportYamlFile = properties.getProperty("report.yaml.directory") + properties.getProperty("report.yaml.file");

        log.info("Loading report from file : {}", reportYamlFile);

        FileReader fileReader = new FileReader(new File( reportYamlFile ));

        for (Object o : yaml.loadAll(fileReader)) {
            Report report = (Report) o;

            log.info("Loaded report from yaml : {}", report);

            //Sets validation on bean
            reportValidator.validate( report );

            reports.add(report);
        }

        return reports;
    }
}
