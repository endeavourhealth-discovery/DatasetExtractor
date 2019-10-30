package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.model.*;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private JpaRepository repository;

    private SFTPUploader sftpUploader;

    private final Properties properties;

    private final Scheduler scheduler;

    public ReportGenerator(Properties properties) {

        this.properties = properties;

        this.sftpUploader = new SFTPUploader();

        this.scheduler = new Scheduler();

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        log.info("**** ReportGenerator successfully booted!!");
    }

    public ReportGenerator(Properties properties, SFTPUploader sftpUploader) {
        this(properties);
        this.sftpUploader = sftpUploader;
    }

    public List<Report> generate(List<Report> reports) {

        for (Report report : reports) {

            if( !reportIsRunnable( report ) ) {
                continue;
            }

            try {
                executeReport(report);

            } catch (Exception e) {
                log.error("Report " + report + " has thrown exception", e);
                report.setErrorMessage( e.getMessage() );
            }

            report.setEndTime(LocalDateTime.now());
        }

        return reports;
    }

    private boolean reportIsRunnable(Report report) {

        if (!report.getActive()) {
            log.warn("Report is inactive");
            return false;
        }
        if (!report.isValid()) {
            log.warn("Report is invalid {}", report.getErrors());
            return false;
        }
        if(!scheduler.isScheduled( report.getSchedule() )) {
            log.info("Report is not scheduled");
            return false;
        }

        return true;
    }


    private void executeReport(Report report) throws Exception {

        log.info("Generating report {}", report);

        bootRepository(report);

        callStoredProcedures(report.getStoredProcedureExecutor().getPreStoredProcedures(), report.getStoredProcedureExecutor());

        executeExtensions( report );

        executeDeltas( report );

        callStoredProcedures(report.getStoredProcedureExecutor().getPostStoredProcedures(), report.getStoredProcedureExecutor());

        exportToCSVFile(report);

        zipAndUploadToSFTP(report);

        processAnalytics( report.getAnalytics() );

        //Not all reports have use of a database
        if(repository != null) repository.close();
    }

    private void processAnalytics(Analytics analytics) {

        if(analytics == null) {
            log.info("No analytics found, nothing to do here");
            return;
        }
        
        if(!analytics.getSwitchedOn()) {
            log.info("Analytics switched off, nothing to do");
        }

        repository.processAnalytics( analytics );
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

        if(!report.requiresDatabase()) {
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

        Path pathToBeDeleted = Paths.get(directory.getAbsolutePath());

        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .filter(f -> !f.getAbsolutePath().equals(directory.getAbsolutePath()))//Don't delete parent
                .forEach(File::delete);
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
