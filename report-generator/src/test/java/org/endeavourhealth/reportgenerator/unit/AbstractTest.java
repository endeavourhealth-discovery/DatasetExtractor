package org.endeavourhealth.reportgenerator.unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractTest {

    protected static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = AbstractTest.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load(inputStream);

        return properties;
    }
}
