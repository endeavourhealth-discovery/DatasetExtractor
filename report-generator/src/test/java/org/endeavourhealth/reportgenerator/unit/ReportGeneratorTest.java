package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class ReportGeneratorTest extends AbstractTest {


    private ReportGenerator reportGenerator;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = loadProperties();

    }


    @Test
    public void perfectRun() throws Exception {

        properties.put("report.yaml.file", "report.unit.yaml");

        ReportGenerator reportGenerator = new ReportGenerator( properties );

        reportGenerator.generate();
    }
}
