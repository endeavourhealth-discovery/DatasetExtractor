package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.slack.SlackReporter;
import org.endeavourhealth.reportgenerator.validator.ReportValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;

@Slf4j
public class ReportRunner {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );

        List<Report> reports = loadReports( properties );

        try (  ReportGenerator reportGenerator = new ReportGenerator( properties ) ) {

            reportGenerator.generate( reports );

            log.info("Report generation all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
        }

        SlackReporter slackReporter = new SlackReporter();

        slackReporter.report( reports );
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
