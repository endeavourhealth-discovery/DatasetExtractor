package org.endeavourhealth.batcher.repository;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.batcher.bean.Database;
import org.endeavourhealth.batcher.model.RemoteObservation;

import javax.persistence.*;
import java.sql.*;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JpaRepository {

    private final EntityManager entityManagerRemote;
    private EntityManagerFactory entityManagerFactoryRemote;
    private EntityManagerFactory entityManagerFactoryLocal;

    private final Properties properties;

    public JpaRepository(Properties properties, Database storedProcedureDatabase) throws SQLException {

        switch (storedProcedureDatabase) {
            case COMPASS:
                properties.put("javax.persistence.jdbc.password", properties.get("db.compass.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.compass.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.compass.url"));
                break;
            case CORE:
                properties.put("javax.persistence.jdbc.password", properties.get("db.core.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.core.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.core.url"));
                break;
            case SUBSCRIBER_PI:
                properties.put("javax.persistence.jdbc.password", properties.get("db.sub.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.sub.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.sub.url"));
                break;
            case PCR:
                properties.put("javax.persistence.jdbc.password", properties.get("db.pcr.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.pcr.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.pcr.url"));
                break;
            case SEL:
                properties.put("javax.persistence.jdbc.password", properties.get("db.sel.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.sel.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.sel.url"));
                break;
        }

        this.properties = properties;

        entityManagerFactoryRemote = Persistence.createEntityManagerFactory("databaseUno", properties);
        entityManagerRemote = entityManagerFactoryRemote.createEntityManager();
        entityManagerFactoryLocal = getEntityManagerFactoryLocal();
    }

    public void close() throws SQLException {

        if(entityManagerFactoryRemote != null) entityManagerFactoryRemote.close();

        if(entityManagerFactoryLocal != null) entityManagerFactoryLocal.close();
    }

    public List<RemoteObservation> getObservationsFromRemote(Integer offset) {

        String sql = "select obs.id, obs.patient_id, obs.original_code, obs.original_term from observation obs limit " + offset + ", 3000";
        Query query = entityManagerRemote.createNativeQuery(sql);

        log.debug("Sql {}", sql);

        return query.getResultList();
    }

    public void insertObservations(List<RemoteObservation> observationsFromRemote) {

        EntityManager entityManagerLocal = entityManagerFactoryLocal.createEntityManager();

        entityManagerLocal.persist(observationsFromRemote);

        entityManagerLocal.close();
    }

    private EntityManagerFactory getEntityManagerFactoryLocal() {
        properties.put("javax.persistence.jdbc.password", properties.get("db.local.password"));
        properties.put("javax.persistence.jdbc.user", properties.getProperty("db.local.user"));
        properties.put("javax.persistence.jdbc.url", properties.getProperty("db.local.url"));

        EntityManagerFactory entityManagerFactoryCore = Persistence.createEntityManagerFactory("coreDatabase", properties);

        return entityManagerFactoryCore;
    }


}
