package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.model.*;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.*;
import org.endeavourhealth.reportgenerator.validator.ReportValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private JpaRepository repository;

    private SFTPUploader sftpUploader;

    private final Properties properties;

    public ReportGenerator(Properties properties) throws Exception {

        this.properties = properties;

        this.sftpUploader = new SFTPUploader();

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        log.info("**** ReportGenerator successfully booted!!");
    }

    public ReportGenerator(Properties properties, SFTPUploader sftpUploader) throws Exception {
        this(properties);
        this.sftpUploader = sftpUploader;
    }

    public List<Report> generate(List<Report> reports) throws Exception {

        for (Report report : reports) {

            if (!report.getActive()) {
                log.warn("Report is inactive");
                continue;
            }
            if (!report.isValid()) {
                log.warn("Report is invalid {}", report.getErrors());
                continue;
            }

            try {
                executeReport(report);
                report.setEndTime(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Report " + report + " has thrown exception", e);
                report.setEndTime(LocalDateTime.now());
                report.setErrorMessage( e.getMessage() );
            }
        }

        return reports;
    }


    private void executeReport(Report report) throws Exception {

        log.info("Generating report {}", report);

        bootRepository(report);

        callStoredProcedures(report.getStoredProcedureExecutor().getPreStoredProcedures(), report.getStoredProcedureExecutor());

        executeExtensions( report );

        executeDeltas( report );

        callStoredProcedures(report.getStoredProcedureExecutor().getPostStoredProcedures(), report.getStoredProcedureExecutor());

        exportToCSVFile(report);

        exportToFihr(report);

        zipAndUploadToSFTP(report);

        //Not all reports have use of a database
        if(repository != null) repository.close();
    }

    private void exportToFihr(Report report) throws Exception {
        FihrExport fihrExport = report.getFihrExport();

        if (fihrExport == null) {
            log.info("No configuration for fihr export found, nothing to do here");
            return;
        }

        if (!fihrExport.getSwitchedOn()) {
            log.info("Fihr switched off, nothing to do here");
            return;
        }

        log.warn("Fihr exporter not implemented yet!!!!!!");
    }

    private void executeDeltas(Report report) {

        if(report.getDelta() == null) {
            log.info("No delta found, nothing to do here");
            return;
        }

        if( !report.getDelta().getSwitchedOn()) {
            log.info("Delta switched off, nothing to do here");
            return;
        }

        DeltaExecutor deltaExecutor = new DeltaExecutor( repository );

        deltaExecutor.execute( report.getDelta() );
    }

    private void executeExtensions(Report report) {

        if(report.getExtensions() == null || report.getExtensions().isEmpty()) {
            log.info("No extensions found, nothing to do here");
            return;
        }

        ExtensionExecutor extensionExecutor = new ExtensionExecutor( repository );

        for(Extension e : report.getExtensions()) {
            extensionExecutor.execute( e );
        }
    }

    private void bootRepository(Report report) throws SQLException {

        if(report.requiresDatabase() == false) {
            log.info("Report doesn't required database, not booting repository");
            return;
        }

        this.repository = new JpaRepository(properties, report.getStoredProcedureExecutor().getDatabase());
    }

    private void zipAndUploadToSFTP(Report report) throws Exception {

        SftpUpload sftpUpload = report.getSftpUpload();

        if(sftpUpload == null) {
            log.info("No configuration for sftp found, nothing to do here");
            return;
        }

        if (!sftpUpload.getSwitchedOn()) {
            log.info("SFTP switched off, nothing to do here");
            return;
        }

        File stagingDirectory = new File(properties.getProperty("csv.staging.directory"));

        cleanOutputDirectory(stagingDirectory);

        FileZipper fileZipper = new FileZipper(report, properties );

        String filenameToSftp = fileZipper.zip();

        File fileToSftp = new File(filenameToSftp);

        FileEncrypter fileEncrypter = new FileEncrypter();

        fileEncrypter.encryptFile(fileToSftp);

        sftpUploader.uploadDirectory(sftpUpload, stagingDirectory);
    }

    private void exportToCSVFile(Report report) throws Exception {

        CSVExport csvExport = report.getCsvExport();

        if (csvExport == null) {
            log.info("No configuration for csv export found, nothing to do here");
            return;
        }

        if (!csvExport.getSwitchedOn()) {
            log.info("CSV switched off, nothing to do here");
            return;
        }

        if (csvExport.getTables().isEmpty()) {
            log.info("CSV configuration found, but no csv tables to export, nothing to do here");
            return;
        }

        File outputDirectory = new File(csvExport.getOutputDirectory());

        cleanOutputDirectory(outputDirectory);

        for (Table table : csvExport.getTables()) {

            Properties properties = getCSVExporterProperties(report, table);

            try (CSVExporter csvExporter = new CSVExporter(properties)) {
                csvExporter.exportCSV();
            }
        }
    }


    private void cleanOutputDirectory(File directory) throws IOException {
      log.info("Deleting all files from directory {}", directory);

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                log.debug("Deleting file: " + file.getName());
                file.delete();
            }
            if (file.isDirectory()) {
                log.debug("Deleting directory: " + file.getName());
                FileUtils.deleteDirectory(file);
            }
        }
    }

    private Properties getCSVExporterProperties(Report report, Table table) {

        Properties p = new Properties();

        switch (report.getStoredProcedureExecutor().getDatabase()) {
            case COMPASS:
                p.put("url", properties.getProperty("db.compass.url"));
                p.put("user", properties.getProperty("db.compass.user"));
                p.put("password", properties.getProperty("db.compass.password"));
                break;
            case CORE:
                p.put("url", properties.getProperty("db.core.url"));
                p.put("user", properties.getProperty("db.core.user"));
                p.put("password", properties.getProperty("db.core.password"));
                break;
            case PCR:
                p.put("url", properties.getProperty("db.pcr.url"));
                p.put("user", properties.getProperty("db.pcr.user"));
                p.put("password", properties.getProperty("db.pcr.password"));
                break;
        }

        CSVExport csvExport = report.getCsvExport();

        p.put("outputDirectory", csvExport.getOutputDirectory());
        p.put("noOfRowsInEachDatabaseFetch", "50000");

        p.put("dbTableName", table.getName());
        p.put("csvFilename", table.getFileName());
        p.put("noOfRowsInEachOutputFile", csvExport.getMaxNumOfRowsInEachOutputFile().toString());

        return p;
    }

    private void callStoredProcedures(List<String> storedProcedures, StoredProcedureExecutor storedProcedureExecutor) {

        if(!storedProcedureExecutor.getSwitchedOn()) {
            log.info("Stored procedure execution is turned off");
            return;
        }

        if (storedProcedures == null) {
            log.info("No stored procedures in report definition");
            return;
        }

        log.info("Cycling through stored procedures");

        for (String storedProcedure : storedProcedures) {
            repository.call(storedProcedure, storedProcedureExecutor);
        }

        log.info("Stored procedures all called");
    }

    @Override
    public void close() throws Exception {
        if(repository != null) repository.close();
    }
}
