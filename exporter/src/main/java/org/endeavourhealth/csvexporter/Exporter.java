package org.endeavourhealth.csvexporter;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.csvexporter.repository.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public abstract class Exporter implements AutoCloseable {

    protected Repository repository;

    protected String outputDirectory;

    protected int fileCount = 0;

    protected int noOfRowsInEachOutputFile;

    protected int noOfRowsInEachDatabaseFetch;

    protected int pageSize;

    protected String dbTableName;

    protected String filename;


    protected String buildFilename(String csvFilename) {
        if(filename.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            return  filename.replace("{today}", today);
        }

        return filename;
    }

    @Override
    public void close() throws Exception {

        closeExporter();

        repository.close();
    }

    protected abstract void closeExporter() throws IOException;
    protected abstract void export() throws Exception;
}
