package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.model.*;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.FileEncrypter;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private JpaRepository repository;

    private List<Report> reports = new ArrayList<>();

    private SFTPUploader sftpUploader;

    private final Properties properties;

    public ReportGenerator(Properties properties) throws Exception {

        this.properties = properties;

        this.sftpUploader = new SFTPUploader();

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        loadReports(properties);

        log.info("**** ReportGenerator successfully booted!!");
    }

    public ReportGenerator(Properties properties, SFTPUploader sftpUploader) throws Exception {
        this(properties);
        this.sftpUploader = sftpUploader;
    }

    public void generate() throws Exception {

        for (Report report : reports) {

            if (!report.getActive()) continue;

            executeReport(report);
        }
    }

    private void executeReport(Report report) throws Exception {

        log.info("Generating report {}", report);

        bootRepository(report);

        callStoredProcedures(report.getStoredProcedureExecutor().getPreStoredProcedures(), report.getStoredProcedureExecutor());

        deanonymise(report);

        callStoredProcedures(report.getStoredProcedureExecutor().getPostStoredProcedures(), report.getStoredProcedureExecutor());

        exportToCSVFile(report);

        uploadToSFTP(report);

        report.setSuccess(true);

        repository.close();
    }

    private void bootRepository(Report report) throws SQLException {
        this.repository = new JpaRepository(properties, report.getStoredProcedureExecutor().getDatabase());
    }

    private void uploadToSFTP(Report report) throws Exception {

        SftpUpload sftpUpload = report.getSftpUpload();

        if(sftpUpload == null) {
            log.info("No configuration for sftp found, nothing to do here");
            return;
        }

        if (!sftpUpload.getSwitchedOn()) {
            log.info("Upload to sftp switched off");
            return;
        }

        File stagingDirectory = new File(properties.getProperty("csv.staging.directory"));

        cleanOutputDirectory(stagingDirectory);

        String filenameToSftp = zipDirectory(report);

        File fileToSftp = new File(filenameToSftp);

        FileEncrypter fileEncrypter = new FileEncrypter();

        fileEncrypter.encryptFile(fileToSftp);

        sftpUploader.uploadDirectory(sftpUpload, stagingDirectory);
    }

    private void exportToCSVFile(Report report) throws Exception {

        CSVExport csvExport = report.getCsvExport();

        if (csvExport == null ) {
            log.info("No configuration for csv export found, nothing to do here");
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

    public String zipDirectory(Report report) throws Exception {

        CSVExport csvExport = report.getCsvExport();

        File source = new File(csvExport.getOutputDirectory());
        File staging = new File(properties.getProperty("csv.staging.directory"));

        log.debug("Compressing contents of: " + source.getAbsolutePath());

        ZipFile zipFile = new ZipFile(staging + File.separator + source.getName() + ".zip");

        log.info("Creating file: " + zipFile.getFile().getAbsolutePath());

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setIncludeRootFolder(false);

        zipFile.createZipFileFromFolder(source, parameters, true, 10485760);

        return zipFile.getFile().getAbsolutePath();
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


    private void deanonymise(Report report) {

        if(!report.getRequiresDeanonymising()) return;

        log.info("Report required deanonymising, running...");

        repository.bootEntityManagerFactoryCore();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymise(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIds(offset);
        }

        log.info("...deanonymising all done");
    }

    private void loadReports(Properties properties) throws FileNotFoundException {

        Yaml yaml = new Yaml(new Constructor(Report.class));

        FileReader fileReader = new FileReader(new File(properties.getProperty("report.yaml.file")));

        for (Object o : yaml.loadAll(fileReader)) {
            Report report = (Report) o;

            log.info("Loaded report from yaml : {}", report);

            reports.add(report);
        }
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
