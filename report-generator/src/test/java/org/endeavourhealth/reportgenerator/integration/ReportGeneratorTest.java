package org.endeavourhealth.reportgenerator.integration;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.junit.*;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;

import java.util.Properties;

import static org.mockito.Mockito.doNothing;

public class ReportGeneratorTest extends AbstractTest {

    @ClassRule
//    public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer("mysql:5.7").withInitScript("data_extracts.sql");
    public static GenericContainer mysql = new GenericContainer<>("devdb-ceg")
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "admin");

    private ReportGenerator reportGenerator;
    private Properties properties;


    @Before
    public void init() throws Exception {

        mysql.start();

        String address = mysql.getContainerIpAddress();
        Integer port = mysql.getFirstMappedPort();

        properties = loadProperties();

        properties.put("db.core.url", "jdbc:mysql://localhost:" + port + "/data_extracts" );
        properties.put("db.core.user", "root" );
        properties.put("db.core.password", "admin" );

        properties.put("db.compass.url", "jdbc:mysql://localhost:" + port + "/data_extracts" );
        properties.put("db.compass.user", "root" );
        properties.put("db.compass.password", "admin" );

        properties.put("db.pcr.url", "jdbc:mysql://localhost:" + port + "/data_extracts" );
        properties.put("db.pcr.user", "root" );
        properties.put("db.pcr.password", "admin" );

        properties.put("report.yaml.directory", "/home/hal/dev/data_extracts/" );
        properties.put("report.yaml.file", "reports.yaml" );
        properties.put("csv.staging.directory", "admin" );

        properties.put("slack.url", "url" );
        properties.put("slack.switched.on", "/home/hal/dev/data_extracts/staging" );

        SFTPUploader sftpUploader = Mockito.mock(SFTPUploader.class);

        doNothing().when(sftpUploader);

        reportGenerator = new ReportGenerator(properties, sftpUploader);
    }

    @Test
    public void getRecords() throws Exception {
//        reportGenerator.generate(reports);
    }
}
