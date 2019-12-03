package org.endeavourhealth.csvexporter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.endeavourhealth.csvexporter.repository.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ExcelExporter extends Exporter {

    private Workbook workbook;

    private Sheet sheet;


    public ExcelExporter(Properties properties) throws Exception {
        this(properties, new Repository(properties));
    }

    public ExcelExporter(Properties properties, Repository repository) {

        this.repository = repository;

        log.info("**** Booting CSVExporter, loading property file and db repository.....");

        outputDirectory = properties.getProperty("outputDirectory");

        filename = buildFilename( properties.getProperty("csvFilename") );

        dbTableName = properties.getProperty("dbTableName");

        noOfRowsInEachOutputFile = Integer.valueOf( properties.getProperty("noOfRowsInEachOutputFile") );

        noOfRowsInEachDatabaseFetch =  Integer.valueOf( properties.getProperty("noOfRowsInEachDatabaseFetch") );

        if(noOfRowsInEachOutputFile > 0) {
          pageSize = noOfRowsInEachOutputFile < noOfRowsInEachDatabaseFetch ? noOfRowsInEachOutputFile : noOfRowsInEachDatabaseFetch;
        } else {
          pageSize = noOfRowsInEachDatabaseFetch;
        }

        log.info("Exporting db table {} to file {} to directory", dbTableName, filename, outputDirectory);

        log.info("noOfRowsInEachDatabaseFetch = {}", noOfRowsInEachDatabaseFetch);
        log.info("noOfRowsInEachOutputFile = {}", noOfRowsInEachOutputFile);

        log.info("**** CSVExporter successfully booted!!");
    }

    public void export() throws Exception {

        fileCount = 0;

        int currentFileCount = 0, offset = 0;

        bootNewExcelWorkbook();

        int rowCount = 1;

        List<List<String>> result = repository.getRows(offset, pageSize);

        while(result.size() > 0) {

            for (int i = 0; i < result.size(); i++) {
                List<String> values = result.get(i);
                Row row = sheet.createRow( rowCount++ );

                for(String s : values) {
                    row.createCell(i).setCellValue(s);
                }
            }

            offset += result.size();

            currentFileCount += result.size();

            //noOfRowsInEachOutputFile == 0 or smaller, no limit
            if(currentFileCount > noOfRowsInEachOutputFile && noOfRowsInEachOutputFile > 0) {

                saveWorkbookToFile();

                bootNewExcelWorkbook();

                currentFileCount = 0;
            }

            result = repository.getRows(offset, pageSize);

            log.info("No of rows processed {}", offset);
        }

    // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("poi-generated-file.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();

        log.info("Finished writing csv");
    }

    private void bootNewExcelWorkbook() throws SQLException {

        workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("report");

        attachHeaderRow( sheet );
    }

    private void saveWorkbookToFile() throws IOException {
        String outputFileName = fileCount == 0 ?  outputDirectory + filename + ".xlsx" : outputDirectory  + filename + fileCount + ".xlsx";

        log.info("Opening file {} for writing.....", outputFileName);

        FileOutputStream fileOut = new FileOutputStream("poi-generated-file.xlsx");
        workbook.write(fileOut);

        fileOut.close();
        workbook.close();
    }

    private void attachHeaderRow(Sheet sheet) throws SQLException {

        String[] headers = repository.getHeaders();

        log.debug("With headers {}", Arrays.toString( headers ));

        Row headerRow = sheet.createRow(0);

        for(int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
//            cell.setCellStyle(headerCellStyle);
        }
    }


    @Override
    protected void closeExporter() throws IOException {

    }
}
