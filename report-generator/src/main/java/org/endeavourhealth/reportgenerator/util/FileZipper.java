package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
public class FileZipper {

    private final File source;

    private final File staging;

    private String fileName;

    public FileZipper(Report report, Properties properties) {
        this.source = new File(report.getCsvExport().getOutputDirectory());
        this.staging = new File(properties.getProperty("csv.staging.directory"));
        this.fileName = report.getSftpUpload().getZipFilename() == null ? source.getName() : report.getSftpUpload().getZipFilename();
        this.fileName = checkForExpressions(this.fileName);
    }

    public String zip() throws Exception {

        log.debug("Compressing contents of: " + source.getAbsolutePath());

        ZipFile zipFile = new ZipFile(staging + File.separator + fileName + ".zip");

        String absolutePath = zipFile.getFile().getAbsolutePath();

        log.info("Creating file: " + absolutePath);

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setIncludeRootFolder(false);

        zipFile.createZipFileFromFolder(source, parameters, true, 10485760);

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

