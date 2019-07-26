package org.endeavourhealth.csvexporter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.csvexporter.repository.Repository;

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

    private final String outputDirectory;

    private int fileCount = 0;

    private final int noOfRowsInEachOutputFile;

    private final int noOfRowsInEachDatabaseFetch;

    private final int pageSize;

    private String tableName;

    public CSVExporter(Properties properties) throws Exception {
        this(properties, new Repository(properties));
    }

    public CSVExporter(Properties properties, Repository repository) {

        this.repository = repository;

        log.info("**** Booting CSVExporter, loading property file and db repository.....");

        outputDirectory = properties.getProperty("outputDirectory");

        noOfRowsInEachOutputFile = Integer.valueOf( properties.getProperty("noOfRowsInEachOutputFile") );

        noOfRowsInEachDatabaseFetch =  Integer.valueOf( properties.getProperty("noOfRowsInEachDatabaseFetch") );

        pageSize = noOfRowsInEachOutputFile < noOfRowsInEachDatabaseFetch ? noOfRowsInEachOutputFile : noOfRowsInEachDatabaseFetch;

        log.info("**** CSVExporter successfully booted!!");
    }

    public void exportCSV(String tableName, String fileName) throws Exception {

        this.tableName = tableName;

        fileCount = 0;

        bootNewPrintWriter( fileName );

        int currentFileCount = 0, offset = 0;

        repository.setTableName(tableName);

        List<List<String>> result = repository.getRows(offset, pageSize);

        while(result.size() > 0) {

            csvPrinter.printRecords( result );

            offset += result.size();

            currentFileCount += result.size();

            if(currentFileCount > noOfRowsInEachOutputFile) {

                csvPrinter.close( true );

                writer.close();

                bootNewPrintWriter( fileName );

                currentFileCount = 0;
            }

            result = repository.getRows(offset, pageSize);

            log.info("No of rows processed {}", offset);
        }

        log.info("Finished writing csv");
    }

    private void bootNewPrintWriter(String filename) throws Exception {

        String outputFileName = fileCount == 0 ?  outputDirectory + filename + ".csv" : outputDirectory  + filename + fileCount + ".csv";

        log.info("Opening file {} for writing.....", outputFileName);
        log.debug("With headers {}", repository.getHeaders());

        writer = Files.newBufferedWriter(Paths.get( outputFileName ));

        fileCount++;

        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader( repository.getHeaders() ));
    }



    @Override
    public void close() throws Exception {
        csvPrinter.close( true );

        writer.close();

        repository.close();
    }

    public void exportCSV(String tableName) throws Exception {
        exportCSV(tableName, tableName);
    }
}
