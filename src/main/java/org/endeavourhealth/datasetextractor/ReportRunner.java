package org.endeavourhealth.datasetextractor;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.datasetextractor.exception.DatasetExtractorException;
import org.endeavourhealth.datasetextractor.exception.ReportGeneratorException;
import org.endeavourhealth.datasetextractor.repository.JpaRepository;
import org.endeavourhealth.datasetextractor.repository.Repository;

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

//            reportGenerator.generate();

            reportGenerator.deanonymise();

            log.info("...all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
        }
    }

    private static Properties loadProperties(String[] args) throws IOException {

//        if(args.length == 0) throw new ReportGeneratorException("Required args is absent [tablename]");

        Properties properties = new Properties();

        InputStream inputStream = ReportRunner.class.getClassLoader().getResourceAsStream("data.extractor.properties");
        InputStream inputStream2 = ReportRunner.class.getClassLoader().getResourceAsStream("data.extractor.properties");

        properties.load( inputStream );

        return properties;
    }

}
