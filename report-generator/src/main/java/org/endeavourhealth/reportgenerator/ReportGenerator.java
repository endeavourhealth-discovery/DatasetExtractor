package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.csv.CSVDeltaExporter;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.FileEncrypter;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
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

    public ReportGenerator(Properties properties) throws Exception {

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

        callStoredProcedures(report);

        deanonymise(report);

        List<Delta> deltas = generateDelta(report);

        csvDeltaExporter.exportCsv(report, deltas);

        fileEncrypter.encryptFile( report );

        sftpUploader.upload( report );

        repository.renameTable(report);

        report.setSuccess(true);
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

    private void callStoredProcedures(Report report) {

        log.info("Cycling through stored procedures");

        for (String storedProcedure : report.getStoredProcedures()) {
            repository.call(storedProcedure);
        }

        log.info("Stored procedures all called");
    }


    private void deanonymise(Report report) {

        if (!report.getRequiresDeanonymising()) return;

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset);

        while (pseudoIds.size() > 0) {

            List<Object[]> rows = repository.deanonymise(pseudoIds);

            offset += 1000;

            pseudoIds = repository.getPseudoIds(offset);
        }
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
