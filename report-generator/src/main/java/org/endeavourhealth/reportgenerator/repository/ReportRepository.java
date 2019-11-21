package org.endeavourhealth.reportgenerator.repository;

import org.endeavourhealth.reportgenerator.model.Report;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Properties;

public class ReportRepository implements AutoCloseable {

    private  EntityManagerFactory entityManagerFactory;

    ReportRepository() {

    }

    public ReportRepository(Properties properties) {

        this();

        properties.put("javax.persistence.jdbc.password", properties.get("db.report.password"));
        properties.put("javax.persistence.jdbc.user", properties.getProperty("db.report.user"));
        properties.put("javax.persistence.jdbc.url", properties.getProperty("db.report.url"));

        entityManagerFactory = Persistence.createEntityManagerFactory("reportDatabase", properties);
    }

    public void save(List<Report> reports) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        for (Report report : reports) {
            entityManager.persist( report );
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public void close() throws Exception {

        entityManagerFactory.close();

    }
}
