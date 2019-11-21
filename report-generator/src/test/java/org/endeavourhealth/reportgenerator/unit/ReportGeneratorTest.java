package org.endeavourhealth.reportgenerator.unit;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.SFTPUploader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)
public class ReportGeneratorTest extends AbstractTest {


    @Mock
    private SFTPUploader sftpUploader;

    @Mock
    private JpaRepository repository;

    @InjectMocks
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

//        reportGenerator.generate(reports);
    }
}
