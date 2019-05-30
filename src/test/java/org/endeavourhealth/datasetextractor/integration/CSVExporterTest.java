package org.endeavourhealth.datasetextractor.integration;

import org.endeavourhealth.datasetextractor.CSVExporter;
import org.endeavourhealth.datasetextractor.Extractor;
import org.endeavourhealth.datasetextractor.repository.Repository;
import org.junit.*;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVExporterTest {

    @ClassRule
    public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer("mysql:5.7").withInitScript("data.sql");

    private CSVExporter csvExporter;
    private Properties properties;


    @Before
    public void init() throws Exception {

        mysql.start();

        properties = loadProperties();

        properties.put("url",  mysql.getJdbcUrl() );
        properties.put("user",  mysql.getUsername() );
        properties.put("password",  mysql.getPassword() );

        Repository repository = new Repository(properties);

        csvExporter = new CSVExporter(properties, repository);
    }


    @Test
    public void getRecords() throws Exception {

        csvExporter.exportCSV();

        //Must flush to test otherwise file isn't written to
        csvExporter.close();

        //Test
        String outputDirectory = properties.getProperty("outputFilepath");

        String output = new String(Files.readAllBytes(Paths.get(outputDirectory + "0.csv")));

        Assert.assertEquals("a,b,c,d,e\r\n" +
                "1,2,3,4,5\r\n" +
                "1,2,3,4,5\r\n" +
                "1,2,3,4,5\r\n" +
                "1,2,3,4,5\r\n" +
                "1,2,3,4,5\r\n", output);

    }

    @After
    public void after() throws Exception {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = Extractor.class.getClassLoader().getResourceAsStream("data.extractor.properties");

        properties.load( inputStream );

        return properties;
    }
}
