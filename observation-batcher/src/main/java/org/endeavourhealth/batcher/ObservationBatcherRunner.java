package org.endeavourhealth.batcher;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.batcher.repository.JpaRepository;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

@Slf4j
public class ObservationBatcherRunner {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );



        try (  ObservationBatcher observationBatcher = new ObservationBatcher( properties ) ) {

            observationBatcher.batch();

            log.info("Report generation all done!");

        } catch (Exception e) {
            log.error("Exception during report generator", e);
        }
    }

    private static Properties loadProperties(String[] args) throws IOException {

        Properties properties = new Properties();

        InputStream inputStream = ObservationBatcherRunner.class.getClassLoader().getResourceAsStream("batch.properties");

        properties.load( inputStream );

        if(args.length > 0) {

        }

        if(args.length > 1) {
        }


        return properties;
    }


}
