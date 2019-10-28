package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.endeavourhealth.reportgenerator.model.CSVExport;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.SftpUpload;
import org.endeavourhealth.reportgenerator.util.FileZipper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class FileZipperTest extends AbstractTest {


    private Report report;
    private Properties properties;
    private static File zipFile;

    @Before
    public void setUp() throws Exception {
        properties = loadProperties();

        report = new Report();

        SftpUpload sftpUpload = new SftpUpload();
        sftpUpload.setZipFilename("zipit_{today}");

        CSVExport csvExport = new CSVExport();
        csvExport.setOutputDirectory("/home/hal/dev/data_extracts/file_zipper/");

        report.setSftpUpload(sftpUpload);
        report.setCsvExport(csvExport);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(zipFile != null) {
            zipFile.delete();
        }
    }


    @Test
    public void perfectRun() throws Exception {

        properties.put("csv.staging.directory", "/home/hal/dev/data_extracts/file_zipper_output/");

        FileZipper fileZipper = new FileZipper(report, properties);

        String filePath = fileZipper.zip();

        zipFile = new File(filePath);

        assertThat( zipFile  )
                .exists()
                .isFile();

    }
}

