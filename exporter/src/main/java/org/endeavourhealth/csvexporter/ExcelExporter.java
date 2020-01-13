package org.endeavourhealth.csvexporter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.endeavourhealth.csvexporter.repository.Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ExcelExporter extends Exporter {

    private Workbook workbook;

    private Sheet sheet;

    private final String password;

    public ExcelExporter(Properties properties) throws Exception {
        this(properties, new Repository(properties));
    }

    public ExcelExporter(Properties properties, Repository repository) {

        this.repository = repository;

        log.info("**** Booting ExcelExporter, loading property file and db repository.....");

        outputDirectory = properties.getProperty("outputDirectory");

        buildFilename( properties.getProperty("filename") );

        dbTableName = properties.getProperty("dbTableName");

        noOfRowsInEachOutputFile = Integer.valueOf( properties.getProperty("noOfRowsInEachOutputFile") );

        noOfRowsInEachDatabaseFetch =  Integer.valueOf( properties.getProperty("noOfRowsInEachDatabaseFetch") );

        if(noOfRowsInEachOutputFile > 0) {
          pageSize = noOfRowsInEachOutputFile < noOfRowsInEachDatabaseFetch ? noOfRowsInEachOutputFile : noOfRowsInEachDatabaseFetch;
        } else {
          pageSize = noOfRowsInEachDatabaseFetch;
        }

        password = properties.getProperty("excelPassword");

        log.info("Exporting db table {} to file {} to directory {} with password {}", dbTableName, filename, outputDirectory, password);

        log.info("noOfRowsInEachDatabaseFetch = {}", noOfRowsInEachDatabaseFetch);
        log.info("noOfRowsInEachOutputFile = {}", noOfRowsInEachOutputFile);

        log.info("**** ExcelExporter successfully booted!!");
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

                for (int j = 0; j < values.size(); j++) {
                    row.createCell(j).setCellValue( values.get( j ) );
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

        if(currentFileCount > 0) {
          saveWorkbookToFile();
        }

        log.info("Finished writing excel");
    }

    private void bootNewExcelWorkbook() throws SQLException {

        workbook = new XSSFWorkbook();

        sheet = workbook.createSheet("report");

        attachHeaderRow( sheet );
    }

    private void encrypt(String outputFileName) throws IOException, GeneralSecurityException, InvalidFormatException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {

            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            // EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile, CipherAlgorithm.aes192, HashAlgorithm.sha384, -1, -1, null);
            Encryptor enc = info.getEncryptor();
            enc.confirmPassword( password );
            // Read in an existing OOXML file and write to encrypted output stream
            // don't forget to close the output stream otherwise the padding bytes aren't added
            try (OPCPackage opc = OPCPackage.open(new File(outputFileName), PackageAccess.READ_WRITE);
                 OutputStream os = enc.getDataStream(fs)) {
                opc.save(os);
            }

            outputFileName = outputFileName.replace("unenc", "xlsx");

            log.info("Creating encrypted file {}", outputFileName);

            // Write out the encrypted version
            try (FileOutputStream fos = new FileOutputStream(outputFileName)) {
                fs.writeFilesystem(fos);
            }
        }
    }

    private void saveWorkbookToFile() throws IOException, GeneralSecurityException, InvalidFormatException {

        String outputFileName = fileCount == 0 ?  outputDirectory + filename + ".xlsx" : outputDirectory  + filename + fileCount + ".xlsx";

        if(password != null) {
            outputFileName = fileCount == 0 ?  outputDirectory + filename + ".unenc" : outputDirectory  + filename + fileCount + ".unenc";
        }

        log.info("Creating unencrypted file {}", outputFileName);

        FileOutputStream fileOut = new FileOutputStream( outputFileName );
        workbook.write(fileOut);

        fileOut.close();
        workbook.close();

        if(password != null) {
            encrypt(outputFileName);
            //Delete unencrypted file
            log.debug("Deleting unencrypted file {}", outputFileName);
            Files.delete(Paths.get( outputFileName) );
        }
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
