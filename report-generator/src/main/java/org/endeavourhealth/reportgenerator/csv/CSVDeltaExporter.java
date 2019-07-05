package org.endeavourhealth.reportgenerator.csv;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.reportgenerator.beans.Delta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Slf4j
public class CSVDeltaExporter {

    private BufferedWriter writer;

    private CSVPrinter csvPrinter;

    public CSVDeltaExporter(Properties properties) {

    }

    public void exportCsv(List<Delta> deltas) throws IOException {

        String filename = "/home/hal/test.csv";

        log.info("Opening file {} for writing.....", filename);

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

        for (Delta delta : deltas) {
            csvPrinter.print(delta.getType());
            for (String s : delta.getRow()) {
                csvPrinter.print(s);
            }
            csvPrinter.println();
        }

    }

    public void close() throws IOException {
        if(csvPrinter != null) csvPrinter.close(true);
        if(writer != null) writer.close();
    }
}
