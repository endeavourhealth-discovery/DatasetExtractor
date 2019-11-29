package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
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

    private String fileName;

    private final Zipper zipper;

    public FileZipper(Report report, Properties properties) {
        this.source = getSource( report );
        this.staging = new File(properties.getProperty("csv.staging.directory"));
        this.fileName = getFileName( report );

        this.zipper = report.getZipper();

    }

    private String getFileName(Report report) {

        String filename = source.getName();

        if(report.getZipper() != null && report.getZipper().getZipFilename() != null) {
            filename = report.getZipper().getZipFilename();
        }

        return checkForExpressions(filename);
    }

    private File getSource(Report report) {
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

        String zipFileDirectory = staging + File.separator + fileName + ".zip";

        ZipFile zipFile;

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setIncludeRootFolder(false);

        if(zipper.requiresPassword()) {
           log.info("Using password with {} ", zipper.getEncryptionMethod());
           zipParameters.setEncryptFiles(true);
           zipParameters.setEncryptionMethod(zipper.getEncryptionMethod());
           zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
           zipFile = new ZipFile(zipFileDirectory, zipper.getPassword().toCharArray());
        } else {
           log.info("No password required");
           zipFile = new ZipFile(zipFileDirectory);
        }

        String absolutePath = zipFile.getFile().getAbsolutePath();

        log.info("Creating file: " + absolutePath);

        if(zipper.getSplitFiles()) {
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
