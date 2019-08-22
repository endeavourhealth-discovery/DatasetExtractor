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

import java.io.*;
import java.util.Properties;

public class SftpUploaderTest extends AbstractTest {

    @ClassRule
    public static GenericContainer sftp = new GenericContainer<>("atmoz/sftp")
            .withExposedPorts(22)
            .withClasspathResourceMapping("/docker/ssh_host_rsa_key", "/etc/ssh/ssh_host_rsa_key", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/docker/ssh_host_rsa_key.pub", "/home/foo/.ssh/keys/id_rsa.pub", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/docker/sftp", "/home/foo/ftp/", BindMode.READ_WRITE)
            .withCommand("foo::1000");

    private SFTPUploader sftpUploader;


    private Properties properties;

    @Before
    public void init() throws Exception {

        sftp.start();

        properties = loadProperties();

        sftpUploader = new SFTPUploader( );
    }

    @Test
    public void getRecords() throws Exception {

        Report report = getReport();

        sftpUploader.upload(report);
    }

    private Report getReport() throws FileNotFoundException {

        String address = sftp.getContainerIpAddress();
        
        Integer port = sftp.getMappedPort(22);

        Yaml yaml = new Yaml(new Constructor(Report.class));

        File yamlfile = new File(properties.getProperty("report.yaml.file"));

        Reader yamlReader = new FileReader(yamlfile);

        Report report = yaml.load(yamlReader);

        report.setSftpHostname(address);
        report.setSftpPort(port);

        return report;
    }
}
