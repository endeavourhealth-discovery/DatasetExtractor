package org.endeavourhealth.reportgenerator.integration;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SftpTest {

    @ClassRule
    public static GenericContainer sftp = new GenericContainer<>("atmoz/sftp")
            .withExposedPorts(22)
//            .withClasspathResourceMapping("/docker/ssh_host_rsa_key",
//                    "/etc/ssh/ssh_host_rsa_key",
//                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("/docker/ssh_host_rsa_key.pub",
                    "/home/foo/.ssh/keys/id_rsa.pub",
                    BindMode.READ_ONLY)
            .withCommand("foo::1001");

    private SFTPUploader sftpUploader;


    @Before
    public void init() throws Exception {

        sftp.start();

        sftpUploader = new SFTPUploader();
    }

    @Test
    public void getRecords() throws Exception {

        Report report = getReport();

        sftpUploader.upload(report);
    }

    private Report getReport() {

        String address = sftp.getContainerIpAddress();
        Integer port = sftp.getMappedPort(22);

        Report report = new Report();
        report.setSftpHostname(address);
        report.setSftpPort(port);
        report.setSftpUsername("foo");
        report.setSftpPrivateKeyFile("/home/git/endeavour/DatasetExtractor/report-generator/src/test/etc/docker/ssh_host_rsa_key");

        return report;
    }

    @After
    public void after() throws Exception {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = SftpTest.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load( inputStream );

        return properties;
    }
}
