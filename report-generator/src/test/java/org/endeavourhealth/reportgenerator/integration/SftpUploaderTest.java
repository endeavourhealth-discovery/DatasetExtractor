package org.endeavourhealth.reportgenerator.integration;

import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SftpUploaderTest {

    @ClassRule
    public static GenericContainer sftp = new GenericContainer<>("atmoz/sftp")
            .withExposedPorts(22)
            .withClasspathResourceMapping("/docker/ssh_host_rsa_key.pub", "/home/foo/.ssh/keys/id_rsa.pub", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/docker/sftp", "/home/foo/ftp/", BindMode.READ_WRITE)
            .withCommand("foo::1000");

    private SFTPUploader sftpUploader;


    @Before
    public void init() throws Exception {

        sftp.start();

        Properties properties = loadProperties();

        sftpUploader = new SFTPUploader( properties );
    }

    @Test
    public void getRecords() throws Exception {

        Report report = getReport();

        sftpUploader.upload(report);
    }

    private Report getReport() {

        String address = sftp.getContainerIpAddress();
        Integer port = sftp.getMappedPort(22);

        Yaml yaml = new Yaml(new Constructor(Report.class));

        InputStream yamlInputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("src/main/etc/reports.yaml");

        Report report = yaml.load(yamlInputStream);

        report.setSftpHostname(address);
        report.setSftpPort(port);

        return report;
    }

    @After
    public void after() throws Exception {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = SftpUploaderTest.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load(inputStream);

        return properties;
    }
}
