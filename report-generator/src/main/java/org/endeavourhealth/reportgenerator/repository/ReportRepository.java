package org.endeavourhealth.reportgenerator.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.endeavourhealth.reportgenerator.model.Report;
import org.endeavourhealth.reportgenerator.model.ReportStatus;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReportRepository extends Repository implements AutoCloseable {

    private EntityManagerFactory entityManagerFactory;

    ReportRepository() {}

    public ReportRepository(Properties properties) {
        this();

        properties.put("javax.persistence.jdbc.password", properties.get("db.compass.password"));
        properties.put("javax.persistence.jdbc.user", properties.getProperty("db.compass.user"));
        properties.put("javax.persistence.jdbc.url", properties.getProperty("db.compass.url"));

        entityManagerFactory = Persistence.createEntityManagerFactory("reportDatabase", properties);
    }

    public void save(List<Report> reports) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        reports.stream()
                .filter( r -> r.getStatus() != ReportStatus.NOT_SCHEDULED && r.getStatus() != ReportStatus.INACTIVE)
                .forEach( r -> entityManager.persist(r) );

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public void close() throws Exception {

        entityManagerFactory.close();
    }
}
