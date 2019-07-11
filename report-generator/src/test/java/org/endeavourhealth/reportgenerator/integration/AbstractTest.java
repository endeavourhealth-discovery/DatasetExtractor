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

public abstract class AbstractTest {

    protected static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = AbstractTest.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load(inputStream);

        return properties;
    }
}
