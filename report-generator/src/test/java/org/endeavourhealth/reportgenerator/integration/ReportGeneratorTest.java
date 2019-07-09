package org.endeavourhealth.reportgenerator.integration;

import org.endeavourhealth.reportgenerator.ReportGenerator;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.junit.*;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReportGeneratorTest {

    @ClassRule
//    public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer("mysql:5.7")
//            .withInitScript("data_extracts.sql");
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
        properties.put("db.core.user",  "root" );
        properties.put("db.core.password",  "admin" );
        properties.put("db.compass.url", "jdbc:mysql://localhost:" + port + "/data_extracts" );
        properties.put("db.compass.user",  "root" );
        properties.put("db.compass.password",  "admin" );

        JpaRepository jpaRepository = new JpaRepository(properties);

        reportGenerator = new ReportGenerator(properties, jpaRepository);
    }

    @Test
    public void getRecords() throws Exception {

        reportGenerator.generate();

    }

    @After
    public void after() throws Exception {

    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = ReportGeneratorTest.class.getClassLoader().getResourceAsStream("report.generator.properties");

        properties.load( inputStream );

        return properties;
    }
}
