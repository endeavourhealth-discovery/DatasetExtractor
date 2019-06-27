package org.endeavourhealth.datasetextractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.datasetextractor.exception.DatasetExtractorException;
import org.endeavourhealth.datasetextractor.repository.JpaRepository;
import org.endeavourhealth.datasetextractor.repository.Repository;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private final JpaRepository repository;



    public ReportGenerator(Properties properties, JpaRepository repository) throws Exception {

        this.repository = repository;

        log.info("**** Booting org.endeavourhealth.datasetextractor.ReportGenerator, loading property file and db repository.....");

        log.info("**** ReportGenerator successfully booted!!");
    }

    public void generate() throws Exception {

        String spName = "generateReportForChildImms";

        repository.call( spName );

        log.info("Stored procedure finished");
    }


    @Override
    public void close() throws Exception {

        repository.close();
    }

    public void deanonymise() {

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds( offset );

        while(pseudoIds.size() > 0) {

            List<Object[]> rows = repository.deanonymise( pseudoIds );

            pseudoIds = repository.getPseudoIds(offset);



            offset += 1000;
        }
    }
}
