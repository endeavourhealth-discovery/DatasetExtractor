package org.endeavourhealth.fihrexporter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class FihrExporterRunner {

    public static void main(String... args) throws IOException, SQLException {

        Properties properties = loadProperties( args );

        try (  FihrExporter csvExporter = new FihrExporter( properties  ) ) {

            csvExporter.export();

        } catch (Exception e) {
            //do something!
        }
    }

    private static Properties loadProperties(String[] args) throws IOException {

        Properties properties = new Properties();

        InputStream inputStream = FihrExporterRunner.class.getClassLoader().getResourceAsStream("fihr.exporter.properties");

        properties.load( inputStream );

        return properties;
    }

}
