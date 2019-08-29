package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class ReportRunner {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );

        try (  ReportGenerator reportGenerator = new ReportGenerator( properties ) ) {

            reportGenerator.generate();

            log.info("Report generation all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
        }
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
}
