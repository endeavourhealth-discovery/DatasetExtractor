package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.reportgenerator.model.CSVExport;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.SftpUpload;
import org.endeavourhealth.reportgenerator.model.Zipper;
import org.endeavourhealth.reportgenerator.util.FileZipper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class FileZipperTest extends AbstractTest {


    private Properties properties;
    private static File zipFile;

    @Before
    public void setUp() throws Exception {
        properties = loadProperties();

        properties.put("csv.staging.directory", "/home/hal/dev/data_extracts/file_zipper_output/");
    }

    private Report getReport() {
        Report report = new Report();

        SftpUpload sftpUpload = new SftpUpload();

        Zipper zipper = new Zipper();
        zipper.setZipFilename("zipit_{today}");
        zipper.setSourceDirectory("/home/hal/dev/data_extracts/file_zipper/");

        report.setSftpUpload(sftpUpload);
        report.setZipper(zipper);

        return report;
    }

    @After
    public void tearDown() throws Exception {
        if(zipFile != null) {
            zipFile.delete();
        }
    }


    @Test
    public void perfectRun() throws Exception {

        Report report = getReport();

        FileZipper fileZipper = new FileZipper(report, properties);

        String filePath = fileZipper.zip();

        zipFile = new File(filePath);

        assertThat(zipFile.getName()).matches("zipit_\\d{8}.zip");

        assertThat( zipFile  ).exists().isFile();
    }


    @Test
    public void nullZipper() throws Exception {

        Report report = getReport();

        //Must have csv export output directory set if zipper is null
        CSVExport csvExport = new CSVExport();
        csvExport.setOutputDirectory("/home/hal/dev/data_extracts/file_zipper/");

        report.setZipper(null);
        report.setCsvExport(csvExport);

        FileZipper fileZipper = new FileZipper(report, properties);

        String filePath = fileZipper.zip();

        zipFile = new File(filePath);

        assertThat( zipFile  ).exists().isFile();

        //Clean up
        zipFile.delete();
    }

    @Test
    public void splitFiles() throws Exception {

        Report report = getReport();
        report.getZipper().setSplitFiles(true);

        FileZipper fileZipper = new FileZipper(report, properties);

        String filePath = fileZipper.zip();

        zipFile = new File(filePath);

        assertThat( zipFile  ).exists().isFile();
    }
}

