import org.endeavourhealth.datasetextractor.CSVExporter;
import org.endeavourhealth.datasetextractor.Extractor;
import org.endeavourhealth.datasetextractor.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.*;

public class CSVExporterTest {

    private CSVExporter csvExporter;


    @Before
    public void init() throws Exception {

        Repository repository = mock(Repository.class);

        Properties properties = loadProperties();

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

        csvExporter.exportCSV();
    }

    @After
    public void after() throws Exception {

        csvExporter.close();
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = Extractor.class.getClassLoader().getResourceAsStream("data.extractor.properties");

        properties.load( inputStream );

        return properties;
    }
}
