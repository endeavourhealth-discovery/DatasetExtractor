package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class ReportRunner {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );

        JpaRepository repository = new JpaRepository( properties );

        try (  ReportGenerator reportGenerator = new ReportGenerator( properties, repository ) ) {

            log.info("Starting report generator...");

            reportGenerator.generate();

            log.info("...all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
        }
    }

    private static Properties loadProperties(String[] args) throws IOException {

        Properties properties = new Properties();

        InputStream inputStream = ReportRunner.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load( inputStream );

        return properties;
    }

}
