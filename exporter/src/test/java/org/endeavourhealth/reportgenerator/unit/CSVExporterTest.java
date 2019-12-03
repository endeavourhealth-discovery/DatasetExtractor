package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.csvexporter.CSVExporterRunner;
import org.endeavourhealth.csvexporter.repository.Repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Mockito.*;

public class CSVExporterTest {

    private CSVExporter csvExporter;

    private Properties properties;


    @Before
    public void init() throws Exception {

        Repository repository = mock(Repository.class);

        properties = loadProperties();

        int noOfRowsInEachOutputFile = Integer.valueOf( properties.getProperty("noOfRowsInEachOutputFile") );

        int noOfRowsInEachDatabaseFetch =  Integer.valueOf( properties.getProperty("noOfRowsInEachDatabaseFetch") );

        int pageSize = noOfRowsInEachOutputFile < noOfRowsInEachDatabaseFetch ? noOfRowsInEachOutputFile : noOfRowsInEachDatabaseFetch;

        List<List<String>> result = new ArrayList<>();

        String[] headers = {"1", "2", "3", "4"};

        List<String> row = Arrays.asList( "a", "b", "c", "d");

        result.add( Collections.unmodifiableList( row ));
        result.add( Collections.unmodifiableList( row ));
        result.add( Collections.unmodifiableList( row ));
        result.add( Collections.unmodifiableList( row ));
        result.add( Collections.unmodifiableList( row ));
        result.add( Collections.unmodifiableList( row ));

        when( repository.getRows(0, pageSize) ).thenReturn(result);

        when( repository.getHeaders() ).thenReturn( headers );

        csvExporter = new CSVExporter(properties, repository);
    }


    @Test
    public void getRecords() throws Exception {

        csvExporter.export();

        //Must flush to test otherwise file isn't written to
        csvExporter.close();

        //Test
        String outputDirectory = properties.getProperty("outputFilepath");

        String output = new String(Files.readAllBytes(Paths.get(outputDirectory + "0.csv")));

        Assert.assertEquals(output, "1,2,3,4\r\n" +
                "a,b,c,d\r\n" +
                "a,b,c,d\r\n" +
                "a,b,c,d\r\n" +
                "a,b,c,d\r\n" +
                "a,b,c,d\r\n" +
                "a,b,c,d\r\n");

    }

    @After
    public void after() throws Exception {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = CSVExporterRunner.class.getClassLoader().getResourceAsStream("csv.exporter.properties.example");

        properties.load( inputStream );

        return properties;
    }
}
