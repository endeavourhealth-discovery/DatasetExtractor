package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.csv.CSVDeltaExporter;
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

    private CSVDeltaExporter csvDeltaExporter;

    private SFTPUploader sftpUploader;

    private final FileEncrypter fileEncrypter;

    private final Properties properties;

    public ReportGenerator(Properties properties) throws Exception {

        this.properties = properties;

        this.repository = new JpaRepository( properties );

        this.csvDeltaExporter = new CSVDeltaExporter(properties);

        this.sftpUploader = new SFTPUploader(properties);

        this.fileEncrypter = new FileEncrypter(properties);

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

    private void uploadToSFTP(Report report) {
    }

    private void exportToCSVFile(Report report) throws Exception {

        if(!report.getCsvTablesToExport().isEmpty()) {

            Properties properties = getCSVExporterProperties( report );

            CSVExporter csvExporter = new CSVExporter( properties );

            for(Table table : report.getCsvTablesToExport()) {
                csvExporter.exportCSV( table.getName(), table.getFileName() );
            }
        }
    }
    

    private void cleanOutputDirectory(Report report) throws IOException {

        File outputDirectory = new File( report.getCsvOutputDirectory() );

        for(File file : outputDirectory.listFiles()) {
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
        csvDeltaExporter.close();
    }
}
