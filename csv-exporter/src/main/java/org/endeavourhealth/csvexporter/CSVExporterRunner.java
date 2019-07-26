package org.endeavourhealth.csvexporter;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.csvexporter.exception.CSVExporterException;
import org.endeavourhealth.csvexporter.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class CSVExporterRunner {

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

        if(args.length == 0) throw new CSVExporterException("Required args is absent [tablename]");

        Properties properties = new Properties();

        InputStream inputStream = CSVExporterRunner.class.getClassLoader().getResourceAsStream("csv.exporter.properties");

        properties.load( inputStream );

        properties.put("dbTableName", args[0]);
        properties.put("csvFilename", args[0]);

        if(args.length > 1) properties.put("orderBy", args[1]);

        return properties;
    }

}
