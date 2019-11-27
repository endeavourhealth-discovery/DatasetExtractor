package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.reportgenerator.model.CSVExport;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.SftpUpload;
import org.endeavourhealth.reportgenerator.model.Zipper;
import org.endeavourhealth.reportgenerator.util.FileEncrypter;
import org.endeavourhealth.reportgenerator.util.FileZipper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class FileEncrypterTest extends AbstractTest {


    private Properties properties;
    private static File fileToEncrypt;
    private File stagingDirectory;

    @Before
    public void setUp() throws Exception {
        properties = loadProperties();

        properties.put("csv.staging.directory", "/home/hal/dev/data_extracts/file_zipper_output/");
    }

    private Report getReport() {
        Report report = new Report();


        return report;
    }

    @After
    public void tearDown() throws Exception {
        if(fileToEncrypt != null) {
            fileToEncrypt.delete();
        }
    }


    @Test
    public void perfectRun() throws Exception {

        FileEncrypter fileEncrypter = new FileEncrypter();

        File stagingDirectory = new File(properties.getProperty("csv.staging.directory"));

        fileEncrypter.encryptDirectory( stagingDirectory );

        assertThat(fileToEncrypt.getName()).matches("zipit_\\d{8}.zip");

        assertThat(fileToEncrypt).exists().isFile();
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

        fileToEncrypt = new File(filePath);

        assertThat(fileToEncrypt).exists().isFile();

        //Clean up
        fileToEncrypt.delete();
    }

    @Test
    public void splitFiles() throws Exception {

        Report report = getReport();
        report.getZipper().setSplitFiles(true);

        FileZipper fileZipper = new FileZipper(report, properties);

        String filePath = fileZipper.zip();

        fileToEncrypt = new File(filePath);

        assertThat(fileToEncrypt).exists().isFile();
    }
}

