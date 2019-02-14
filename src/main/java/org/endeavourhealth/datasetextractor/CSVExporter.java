package org.endeavourhealth.datasetextractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Slf4j
public class CSVExporter implements AutoCloseable {

    private final Repository repository;

    private BufferedWriter writer;

    private CSVPrinter csvPrinter;

    private final String filepath;

    private int fileCount = 0;

    private final int noOfRowsInEachOutputFile;

    private final int noOfRowsInEachDatabaseFetch;

    private final int pageSize;


    public CSVExporter(Properties properties, Repository repository) throws Exception {

        this.repository = repository;

        log.info("**** Booting com.example.CSVExporter, loading property file and db repository.....");

        filepath = properties.getProperty("filepath");

        noOfRowsInEachOutputFile = Integer.valueOf( properties.getProperty("noOfRowsInEachOutputFile") );

        noOfRowsInEachDatabaseFetch =  Integer.valueOf( properties.getProperty("noOfRowsInEachDatabaseFetch") );

        pageSize = noOfRowsInEachOutputFile < noOfRowsInEachDatabaseFetch ? noOfRowsInEachOutputFile : noOfRowsInEachDatabaseFetch;

        bootNewPrintWriter();

        log.info("**** CSVExporter successfully booted!!");
    }

    public void exportCSV() throws Exception {

        int currentFileCount = 0, offset = 0;

        List<List<String>> result = repository.getRows(offset, pageSize);

        while(result.size() > 0) {

            csvPrinter.printRecords( result );

            offset += result.size();

            currentFileCount += result.size();

            if(currentFileCount > noOfRowsInEachOutputFile) {

                csvPrinter.close( true );

                writer.close();

                bootNewPrintWriter();

                currentFileCount = 0;
            }

            result = repository.getRows(offset, pageSize);

            log.info("No of rows processed {}", offset);
        }

        log.info("Finished writing csv");
    }

    private void bootNewPrintWriter() throws Exception {

        String filename = filepath + fileCount + ".csv";

        log.info("Opening file {} for writing.....", filename);

        writer = Files.newBufferedWriter(Paths.get( filename ));

        fileCount++;

        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader( repository.getHeaders() ));
    }



    @Override
    public void close() throws Exception {
        csvPrinter.close( true );

        writer.close();

        repository.close();
    }
}
