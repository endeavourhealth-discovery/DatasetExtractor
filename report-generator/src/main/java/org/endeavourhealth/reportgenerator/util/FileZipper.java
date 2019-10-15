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

    private final Report report;

    public FileZipper(Report report, Properties properties) {
        this.source = new File(report.getCsvExport().getOutputDirectory());
        this.staging = new File(  properties.getProperty("csv.staging.directory") );
        this.report = report;
    }

    public String zip() throws Exception {

        log.debug("Compressing contents of: " + source.getAbsolutePath());

        String fileName = getZipFilename();

        ZipFile zipFile = new ZipFile(staging + File.separator + fileName + ".zip");

        log.info("Creating file: " + zipFile.getFile().getAbsolutePath());

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setIncludeRootFolder(false);

        zipFile.createZipFileFromFolder(source, parameters, true, 10485760);

        return zipFile.getFile().getAbsolutePath();
    }

    private String getZipFilename() {

        String filename = report.getSftpUpload().getZipFilename() == null ? source.getName() : report.getSftpUpload().getZipFilename();

        filename = buildFilename(filename);

        return filename;
    }

    private String buildFilename(String filename) {
        if(filename.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            return  filename.replace("{today}", today);
        }

        return filename;
    }
}

