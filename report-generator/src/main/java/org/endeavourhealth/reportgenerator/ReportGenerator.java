package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.Table;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.FileEncrypter;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private final JpaRepository repository;

    private List<Report> reports = new ArrayList<>();

    private SFTPUploader sftpUploader;

    private final Properties properties;

    public ReportGenerator(Properties properties) throws Exception {

        this.properties = properties;

        this.repository = new JpaRepository( properties );

        this.sftpUploader = new SFTPUploader();

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        loadReports( properties );

        log.info("**** ReportGenerator successfully booted!!");
    }

    public ReportGenerator(Properties properties, SFTPUploader sftpUploader) throws Exception {
        this(properties);
        this.sftpUploader = sftpUploader;
    }

    public void generate() throws Exception {

        for (Report report : reports) {

            if(!report.getActive()) continue;

            executeReport(report);
        }
    }

    private void executeReport(Report report) throws Exception {
        log.info("Generating report {}", report);
        
        cleanOutputDirectory( report );

        callStoredProcedures(report.getPreStoredProcedures());

        if (report.getRequiresDeanonymising()) {
            deanonymise(report);
        }

        callStoredProcedures(report.getPostStoredProcedures());

        exportToCSVFile( report );
        
        uploadToSFTP( report );

        report.setSuccess(true);
    }

    private void uploadToSFTP(Report report) throws Exception {

        if(!report.getUploadSftp()) {
            log.debug("Upload to sftp switched off");
        }

        String filenameToSftp = zipDirectory(report);

        File fileToSftp = new File( filenameToSftp );

        FileEncrypter fileEncrypter = new FileEncrypter();

        fileEncrypter.encryptFile( fileToSftp );

        sftpUploader.upload( report, fileToSftp );
    }

    private void exportToCSVFile(Report report) throws Exception {

        if(!report.getCsvTablesToExport().isEmpty()) {

            Properties properties = getCSVExporterProperties( report );

            CSVExporter csvExporter = new CSVExporter( properties );

            for(Table table : report.getCsvTablesToExport()) {
                csvExporter.exportCSV( table.getName(), table.getFileName() );
            }

            csvExporter.close();
        }

    }

    public String zipDirectory(Report report) throws Exception {

        File source = new File( report.getCsvOutputDirectory() );
        File staging = new File( properties.getProperty("csv.staging.directory") );

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
    

    private void cleanOutputDirectory(Report report) throws IOException {

        File outputDirectory = new File( report.getCsvOutputDirectory() );
        File stagingDirectory = new File( report.getCsvStagingDirectory() );

        cleanOutputDirectory( outputDirectory );
        cleanOutputDirectory( stagingDirectory );
    }

    private void cleanOutputDirectory(File directory) throws IOException {
        for(File file : directory.listFiles()) {
            if (file.isFile()) {
                log.debug("Deleting the file: " + file.getName());
                file.delete();
            }
            if (file.isDirectory()) {
                log.debug("Deleting the directory: " + file.getName());
                FileUtils.deleteDirectory(file);
            }
        }
    }

    private Properties getCSVExporterProperties(Report report) {

        Properties p = new Properties();
        p.put("outputDirectory", report.getCsvOutputDirectory());
        p.put("noOfRowsInEachOutputFile", "50000");
        p.put("noOfRowsInEachDatabaseFetch", "1000");
        p.put("url", properties.getProperty("db.compass.url") );
        p.put("user", properties.getProperty("db.compass.user") );
        p.put("password", properties.getProperty("db.compass.password") );

        return p;
    }


    private List<Delta> generateDelta(Report report) {

        List<Delta> additions = repository.getAdditions(report);

        List<Delta> alterations = repository.getAlterations(report);

        List<Delta> deletions = repository.getDeletions(report);

        additions.addAll(alterations);
        additions.addAll(deletions);

        log.debug("Have found {} deltas", additions.size());

        return additions;

    }

    private void callStoredProcedures(List<String> storedProcedures) {

        log.info("Cycling through stored procedures");

        for (String storedProcedure : storedProcedures) {
            repository.call(storedProcedure);
        }

        log.info("Stored procedures all called");
    }


    private void deanonymise(Report report) {

        log.info("Report required deanonymising, running...");

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset);

        while (pseudoIds.size() > 0) {

            List<Object[]> rows = repository.deanonymise(pseudoIds);

            offset += 1000;

            pseudoIds = repository.getPseudoIds(offset);
        }

        log.info("...deanonymising all done");
    }

    private void loadReports(Properties properties) throws FileNotFoundException {

        Yaml yaml = new Yaml(new Constructor(Report.class));

        FileReader fileReader = new FileReader( new File(properties.getProperty("report.yaml.file")) );

        for(Object o : yaml.loadAll(fileReader)) {

            Report report = (Report) o;
            log.info("Found report {}", report);
            reports.add( report );
        }
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
