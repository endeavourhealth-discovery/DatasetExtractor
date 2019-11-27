package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.endeavourhealth.reportgenerator.model.CSVExport;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.Zipper;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
public class FileZipper {

    private final File source;

    private final File staging;

    private final Boolean splitFiles;

    private String fileName;

    private char[] password;

    public FileZipper(Report report, Properties properties) {
        this.source = getSource( report, properties);
        this.staging = new File(properties.getProperty("csv.staging.directory"));
        this.fileName = getFileName( report, properties );

        if(report.getZipper() != null) {
            this.splitFiles = report.getZipper().getSplitFiles();
            this.password = report.getZipper().getPassword();
        } else {
            this.splitFiles = Boolean.FALSE;
        }
    }

    private String getFileName(Report report, Properties properties) {

        String filename = source.getName();

        if(report.getZipper() != null && report.getZipper().getZipFilename() != null) {
            filename = report.getZipper().getZipFilename();
        }

        return checkForExpressions(filename);
    }

    private File getSource(Report report, Properties properties) {
        Zipper zipper = report.getZipper();
        CSVExport csvExport = report.getCsvExport();

        String source = null;

        if(csvExport != null && report.getCsvExport().getOutputDirectory() != null) {
             source = report.getCsvExport().getOutputDirectory();
        }

        if(zipper != null && zipper.getSourceDirectory() != null) {
            source = zipper.getSourceDirectory();
        }

        //No null check as validation should catch any errors

        return new File( source );
    }

    public String zip() throws Exception {

        log.debug("Compressing contents of: " + source.getAbsolutePath());

        ZipFile zipFile = new ZipFile(staging + File.separator + fileName + ".zip");

        String absolutePath = zipFile.getFile().getAbsolutePath();

        log.info("Creating file: " + absolutePath);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setIncludeRootFolder(false);

        if(password != null) {
           zipParameters.setEncryptFiles(true);
           zipParameters.setEncryptionMethod(EncryptionMethod.AES);
           zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        }

        if(splitFiles) {
            zipFile.createSplitZipFileFromFolder(source, zipParameters, true, 10485760); // using 10MB in this example
        } else {
            zipFile.addFolder(source, zipParameters);
        }

        return absolutePath;
    }

    private String checkForExpressions(String filename) {
        if (filename.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            return filename.replace("{today}", today);
        }

        return filename;
    }
}
