package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.csvexporter.ExcelExporter;
import org.endeavourhealth.reportgenerator.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;

@Slf4j
public class Exporter {

    private final Properties properties;
    private final CSVExport csvExport;
    private final ExcelExport excelExport;

    public Exporter(Report report, Properties properties) {
        this.properties = properties;
        csvExport = report.getCsvExport();
        excelExport = report.getExcelExport();
    }

    public void export() throws Exception {
        exportToCSVFile();
        exportToExcelFile();
    }

    private void exportToExcelFile() throws Exception {

        if (excelExport == null) {
            log.info("No configuration for excel export found, nothing to do here");
            return;
        }

        if (!excelExport.getSwitchedOn()) {
            log.info("Excel export switched off, nothing to do here");
            return;
        }

        if (excelExport.getTables().isEmpty()) {
            log.info("Excel configuration found, but no excel tables to export, nothing to do here");
            return;
        }

        File outputDirectory = new File(excelExport.getOutputDirectory());

        cleanDirectory(outputDirectory);

        for (Table table : excelExport.getTables()) {

            Properties properties = getExporterProperties(table, excelExport);

            properties.put("excelPassword", excelExport.getPassword());

            try (ExcelExporter excelExporter = new ExcelExporter(properties)) {
                excelExporter.export();
            }
        }
    }

    private void exportToCSVFile() throws Exception {

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

        cleanDirectory(outputDirectory);

        for (Table table : csvExport.getTables()) {

            Properties properties = getExporterProperties(table, csvExport);

            try (CSVExporter csvExporter = new CSVExporter(properties)) {
                csvExporter.export();
            }
        }
    }

    private Properties getExporterProperties(Table table, Export export) {

        Properties p = new Properties();

        switch ( export.getDatabase() ) {
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

        p.put("outputDirectory", export.getOutputDirectory());
        p.put("noOfRowsInEachDatabaseFetch", "50000");

        p.put("dbTableName", table.getName());
        p.put("filename", table.getFileName());
        p.put("noOfRowsInEachOutputFile", export.getMaxNumOfRowsInEachOutputFile().toString());

        return p;
    }

    private void cleanDirectory(File directory) throws IOException {
        log.info("Deleting all files from directory {}", directory);

        Path pathToBeDeleted = Paths.get(directory.getAbsolutePath());

        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .filter(f -> !f.getAbsolutePath().equals(directory.getAbsolutePath()))//Don't delete parent
                .forEach(File::delete);
    }
}
