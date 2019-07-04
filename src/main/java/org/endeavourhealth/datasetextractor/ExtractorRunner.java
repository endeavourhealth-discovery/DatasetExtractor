package org.endeavourhealth.datasetextractor;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.datasetextractor.exception.DatasetExtractorException;
import org.endeavourhealth.datasetextractor.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class Extractor {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );

        Repository repository = new Repository( properties );

        try (  CSVExporter csvExporter = new CSVExporter( properties, repository ) ) {

            log.info("Starting csv exporter...");

            csvExporter.exportCSV();

            log.info("...all done!");

        } catch (Exception e) {
            log.error("Exception during export", e);
        }
    }

    private static Properties loadProperties(String[] args) throws IOException {

        if(args.length == 0) throw new DatasetExtractorException("Required args is absent [tablename]");

        Properties properties = new Properties();

        InputStream inputStream = Extractor.class.getClassLoader().getResourceAsStream("data.extractor.properties");

        properties.load( inputStream );

        properties.put("tablename", args[0]);

        if(args.length > 1) properties.put("orderBy", args[1]);

        return properties;
    }

}
