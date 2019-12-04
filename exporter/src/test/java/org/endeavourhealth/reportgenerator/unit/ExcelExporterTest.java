package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.csvexporter.CSVExporterRunner;
import org.endeavourhealth.csvexporter.ExcelExporter;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExcelExporterTest {

    private ExcelExporter excelExporter;

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

        excelExporter = new ExcelExporter(properties, repository);
    }


    @Test
    public void export() throws Exception {

        excelExporter.export();

        //Must flush to test otherwise file isn't written to
        excelExporter.close();

        //Test
        String outputDirectory = properties.getProperty("outputDirectory");
        String filename = properties.getProperty("filename");
        String output = new String( Files.readAllBytes(Paths.get(outputDirectory + filename + ".xlsx")) );

//        Assert.assertEquals(8034, output.length());
    }

    @After
    public void after() {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = CSVExporterRunner.class.getClassLoader().getResourceAsStream("csv.exporter.properties.example");

        properties.load( inputStream );

        return properties;
    }
}
