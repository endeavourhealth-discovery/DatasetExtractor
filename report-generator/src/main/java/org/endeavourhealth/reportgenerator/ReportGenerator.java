package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.csv.CSVDeltaExporter;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private final JpaRepository repository;

    private List<Report> reports = new ArrayList<>();

    private CSVDeltaExporter csvDeltaExporter;

    public ReportGenerator(Properties properties, JpaRepository repository) throws Exception {

        this.repository = repository;

        this.csvDeltaExporter = new CSVDeltaExporter( properties );

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        loadReports();

        log.info("**** ReportGenerator successfully booted!!");
    }

    public void generate() throws Exception {

        for(Report report : reports) {

            executeReport( report );
        }
    }

    private void executeReport(Report report) throws IOException {
        log.info("Generating report {}", report);

        callStoredProcedures( report );

        deanonymise( report );

        List<Delta> deltas = generateDelta( report );

//		repository.renameTable( report );

        csvDeltaExporter.exportCsv( deltas );

        report.setSuccess( true );
    }


    private List<Delta> generateDelta(Report report) {

    	List<Delta> additions = repository.getAdditions( report );

    	List<Delta> alterations = repository.getAlterations( report );

        List<Delta> deletions = repository.getDeletions( report );

        additions.addAll(alterations);
        additions.addAll(deletions);

        log.debug("Have found {} deltas", additions.size());

        return additions;

	}

	private void callStoredProcedures(Report report) {

        log.info("Cycling through stored procedures");

        for(String storedProcedure : report.getStoredProcedures()) {
            repository.call( storedProcedure);
        }

        log.info("Stored procedures all called");
    }


    private void deanonymise(Report report) {

        if(!report.getRequiresDeanonymising()) return;

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds( offset );

        while(pseudoIds.size() > 0) {

            List<Object[]> rows = repository.deanonymise( pseudoIds );

            pseudoIds = repository.getPseudoIds(offset);

            offset += 1000;
        }
    }


    private Report loadReports() {

        Yaml yaml = new Yaml(new Constructor(Report.class));

        InputStream yamlInputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("reports.yaml");

        Report report = yaml.load(yamlInputStream);

        log.info("Found report {}", report);

        reports.add(report);

        return report;
    }

    @Override
    public void close() throws Exception {
        repository.close();
        csvDeltaExporter.close();
    }
}
