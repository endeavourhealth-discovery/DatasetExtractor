package org.endeavourhealth.reportgenerator.repository;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.*;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.*;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JpaRepository {

    private EntityManagerFactory entityManagerFactoryPrimary;
    private EntityManagerFactory entityManagerFactorySecondary;

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
        }

        this.properties = properties;

        entityManagerFactoryPrimary = Persistence.createEntityManagerFactory("databaseUno", properties);
    }

    public void bootEntityManagerFactoryCore() {
        entityManagerFactorySecondary = getEntityManagerFactorySecondary();
    }

    public void processAnalytics(Analytics analytics) {

        log.info("Calling analytics {}", analytics);

        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        for (AnalyticItem item : analytics.getItems()) {
            log.debug("Processing {}", item);
            String sql = item.getSql();
            Query query = entityManager.createNativeQuery(sql);
            BigInteger count = (BigInteger) query.getSingleResult();
            String message = item.getMessage().replace("{}", count.toString());
            item.setMessage( message );
        }

        entityManager.close();
    }


    public boolean call(String storedProceduresName, StoredProcedureExecutor storedProcedureExecutor) {

        log.info("Calling stored procedure {} with database {}", storedProceduresName, storedProcedureExecutor.getDatabase());

        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(storedProceduresName);

        boolean execute = query.execute();

        entityManager.close();

        return execute;
    }

    /**
     * CREATE PROCEDURE createDeltaTable (
     *      IN tableName varchar (100),
     *      IN columnsToHash varchar (1000)
     * )
     *
     * CREATE PROCEDURE populateDeltas (
     * 	IN tableName varchar (100)
     * )
     */
    public void call(DeltaTable deltaTable) {

        log.info("Calling delta for {}", deltaTable);

        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        //CREATE PROCEDURE createDeltaTable
        log.debug("Calling createDeltaTable");

        StoredProcedureQuery createDeltaTableQuery = entityManager.createStoredProcedureQuery( "createDeltaTable" );
        createDeltaTableQuery.registerStoredProcedureParameter("tableName", String.class, ParameterMode.IN);
        createDeltaTableQuery.registerStoredProcedureParameter("columnsToHash", String.class, ParameterMode.IN);

        createDeltaTableQuery.setParameter("tableName", deltaTable.getName());
        createDeltaTableQuery.setParameter("columnsToHash", deltaTable.getColumnsToHash());

        createDeltaTableQuery.execute();

        //CREATE PROCEDURE populateDeltas (
        log.debug("Calling populateDeltas");

        StoredProcedureQuery populateDeltasQuery = entityManager.createStoredProcedureQuery( "populateDeltas" );
        populateDeltasQuery.registerStoredProcedureParameter("tableName", String.class, ParameterMode.IN);
        populateDeltasQuery.registerStoredProcedureParameter("uniqueIdentifier", String.class, ParameterMode.IN);
        populateDeltasQuery.registerStoredProcedureParameter("deleteUniqueIdentifier", Boolean.class, ParameterMode.IN);

        populateDeltasQuery.setParameter("tableName", deltaTable.getName());
        populateDeltasQuery.setParameter("uniqueIdentifier", deltaTable.getUniqueIdentifier());
        populateDeltasQuery.setParameter("deleteUniqueIdentifier", deltaTable.getDeleteUniqueIdentifier());

        populateDeltasQuery.execute();

        log.info("Delta table completed");

        entityManager.close();
    }

    public void close() throws SQLException {

        if(entityManagerFactoryPrimary != null) entityManagerFactoryPrimary.close();

        if(entityManagerFactorySecondary != null) entityManagerFactorySecondary.close();
    }

    public List<String> getPseudoIdsForEye(Integer offset) {
        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        String sql = "select distinct pseudo_id from dataset_eye limit " + offset + ", 3000";
        Query query = entityManager.createNativeQuery(sql);

        log.debug("Sql {}", sql);

        return query.getResultList();
    }

    public List<String> getPseudoIds(Integer offset, String tableName) {
        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        String sql = "select distinct pseudo_id from "+ tableName + " limit " + offset + ", 3000";
        Query query = entityManager.createNativeQuery(sql);

        log.debug("Sql {}", sql);

        return query.getResultList();
    }

    public List<String> getPseudoIdsForELGH(Integer offset) {
        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        String sql = "select distinct pseudo_id_from_compass from cohort limit " + offset + ", 3000";
        Query query = entityManager.createNativeQuery(sql);

        log.debug("Sql {}", sql);

        return query.getResultList();
    }

    public List<String> getPseudoIdsForWF(Integer offset) {
        EntityManager entityManager = entityManagerFactoryPrimary.createEntityManager();

        String sql = "select distinct patient_id from dataset_wf limit " + offset + ", 3000";
        Query query = entityManager.createNativeQuery(sql);

        log.trace("Sql {}", sql);

        return query.getResultList();
    }

    public List<Object[]> deanonymiseFrailty(List<String> pseudoIds, String tableName) {

        EntityManager entityManagerCore = entityManagerFactorySecondary.createEntityManager();
        EntityManager entityManagerCompass = entityManagerFactoryPrimary.createEntityManager();

        entityManagerCore.getTransaction().begin();
        entityManagerCompass.getTransaction().begin();

        Query query = entityManagerCore.createNativeQuery("select s.pseudo_id," +
                "p.nhs_number" +
                " from eds.patient_search p " +
                " join subscriber_transform_ceg_enterprise.pseudo_id_map s on p.patient_id = s.patient_id" +
                " where s.pseudo_id in (:pseudoIds) and p.registered_practice_ods_code is not null and p.nhs_number is not null");

        query.setParameter("pseudoIds", pseudoIds);

        List<Object[]> rows = query.getResultList();

        log.debug("Have got {} rows", rows.size());

        Query update = entityManagerCompass.createNativeQuery("update " + tableName + " d set " +
                "d.nhs_number = ? where d.pseudo_id = ?");

        for(Object[] row : rows) {

            update.setParameter(1, row[1]); //nhs_number

            update.setParameter(2, row[0]); //pseudo_id

            update.executeUpdate();

            log.trace("Updating {}", row[0]);
        }

        entityManagerCore.getTransaction().commit();
        entityManagerCore.close();

        entityManagerCompass.getTransaction().commit();
        entityManagerCompass.close();

        return rows;
    }

    public List<Object[]> deanonymiseEYE(List<String> pseudoIds) {

        EntityManager entityManagerCore = entityManagerFactorySecondary.createEntityManager();
        EntityManager entityManagerCompass = entityManagerFactoryPrimary.createEntityManager();

        entityManagerCore.getTransaction().begin();
        entityManagerCompass.getTransaction().begin();

        Query query = entityManagerCore.createNativeQuery("select s.pseudo_id," +
                "p.nhs_number," +
                "p.address_line_1," +
                "p.address_line_2," +
                "p.address_line_3," +
                "p.city," +
                "p.postcode," +
                "p.gender," +
                "p.forenames," +
                "p.surname," +
                "p.date_of_birth" +
                " from eds.patient_search p " +
                " join subscriber_transform_ceg_enterprise.pseudo_id_map s on p.patient_id = s.patient_id" +
                " where s.pseudo_id in (:pseudoIds) and p.registered_practice_ods_code is not null and p.nhs_number is not null");

        query.setParameter("pseudoIds", pseudoIds);

        List<Object[]> rows = query.getResultList();

        log.debug("Have got {} rows", rows.size());

        Query update = entityManagerCompass.createNativeQuery("update dataset_eye d set " +
                "d.NHSNumber = ?," +
                "d.AddressLine1 = ?," +
                "d.AddressLine2 = ?," +
                "d.AddressLine3 = ?," +
                "d.AddressLine4 = ?," +
                "d.Postcode = ?," +
                "d.Gender = ?," +
                "d.FirstName = ?," +
                "d.LastName = ?," +
                "d.BirthDate = ? where d.pseudo_id = ?");

        for(Object[] row : rows) {

            update.setParameter(1, row[1]);
            update.setParameter(2, row[2]);
            update.setParameter(3, row[3]);
            update.setParameter(4, row[4]);
            update.setParameter(5, row[5]);
            update.setParameter(6, row[6]);
            update.setParameter(7, row[7]);
            update.setParameter(8, row[8]);
            update.setParameter(9, row[9]);
            update.setParameter(10, row[10]);

            update.setParameter(11, row[0]); //pseudo_id

            update.executeUpdate();

            log.trace("Updating {}", row[0]);
        }

        entityManagerCore.getTransaction().commit();
        entityManagerCore.close();

        entityManagerCompass.getTransaction().commit();
        entityManagerCompass.close();

        return rows;
    }

    public List<Object[]> deanonymiseELGH(List<String> pseudoIds) {

        EntityManager entityManagerCore = entityManagerFactorySecondary.createEntityManager();
        EntityManager entityManagerCompass = entityManagerFactoryPrimary.createEntityManager();

        entityManagerCore.getTransaction().begin();
        entityManagerCompass.getTransaction().begin();

        Query query = entityManagerCore.createNativeQuery("select distinct s.pseudo_id," +
                "DATE_SUB(p.date_of_birth, INTERVAL DAYOFMONTH(p.date_of_birth) - 1 DAY)," +
                "p.gender," +
                "YEAR(p.date_of_death)" +
                " from eds.patient_search p " +
                " join subscriber_transform_ceg_enterprise.pseudo_id_map s on p.patient_id = s.patient_id" +
                " where s.pseudo_id in (:pseudoIds) and p.registered_practice_ods_code is not null and p.nhs_number is not null");

        query.setParameter("pseudoIds", pseudoIds);

        List<Object[]> rows = query.getResultList();

        log.debug("Have got {} rows", rows.size());

        Query update = entityManagerCompass.createNativeQuery("update cohort c set " +
                "c.DateOfBirth = ?," +
                "c.Gender = ?," +
                "c.YearOfDeath = ?" +
                " where c.pseudo_id_from_compass = ?");

        for(Object[] row : rows) {

            update.setParameter(1, row[1]);
            update.setParameter(2, row[2]);
            update.setParameter(3, row[3]);

            update.setParameter(4, row[0]); //pseudo_id

            update.executeUpdate();

            log.trace("Updating {}", row);
        }

        entityManagerCore.getTransaction().commit();
        entityManagerCore.close();

        entityManagerCompass.getTransaction().commit();
        entityManagerCompass.close();

        return rows;
    }

    public List<Object[]> deanonymiseWF(List<String> pseudoIds) {

        EntityManager entityManagerCore = entityManagerFactorySecondary.createEntityManager();
        EntityManager entityManagerCompass = entityManagerFactoryPrimary.createEntityManager();

        entityManagerCore.getTransaction().begin();
        entityManagerCompass.getTransaction().begin();

        Query query = entityManagerCore.createNativeQuery("select s.enterprise_id," +
                "p.nhs_number," +
                "p.address_line_1," +
                "p.address_line_2," +
                "p.address_line_3," +
                "p.city," +
                "p.postcode," +
                "p.gender," +
                "p.forenames," +
                "p.surname," +
                "p.date_of_birth" +
                " from eds.patient_search p " +
                " join subscriber_transform_ceg_enterprise.enterprise_id_map s on p.patient_id = s.resource_id" +
                " where s.enterprise_id in (:pseudoIds)");

        query.setParameter("pseudoIds", pseudoIds);

        List<Object[]> rows = query.getResultList();

        log.debug("Have got {} rows", rows.size());

        Query update = entityManagerCompass.createNativeQuery("update dataset_wf d set " +
                "d.NHSNumber = ?," +
                "d.AddressLine1 = ?," +
                "d.AddressLine2 = ?," +
                "d.AddressLine3 = ?," +
                "d.City = ?," +
                "d.Postcode = ?," +
                "d.Gender = ?," +
                "d.FirstName = ?," +
                "d.LastName = ?," +
                "d.BirthDate = ? where d.patient_id = ?");

        for(Object[] row : rows) {

            update.setParameter(1, row[1]);
            update.setParameter(2, row[2]);
            update.setParameter(3, row[3]);
            update.setParameter(4, row[4]);
            update.setParameter(5, row[5]);
            update.setParameter(6, row[6]);
            update.setParameter(7, row[7]);
            update.setParameter(8, row[8]);
            update.setParameter(9, row[9]);
            update.setParameter(10, row[10]);

            update.setParameter(11, row[0]); //pseudo_id

            update.executeUpdate();

            log.trace("Updating {}", row[0]);
        }

        entityManagerCore.getTransaction().commit();
        entityManagerCore.close();

        entityManagerCompass.getTransaction().commit();
        entityManagerCompass.close();

        return rows;
    }

    private EntityManagerFactory getEntityManagerFactorySecondary() {
        properties.put("javax.persistence.jdbc.password", properties.get("db.core.password"));
        properties.put("javax.persistence.jdbc.user", properties.getProperty("db.core.user"));
        properties.put("javax.persistence.jdbc.url", properties.getProperty("db.core.url"));

        EntityManagerFactory entityManagerFactoryCore = Persistence.createEntityManagerFactory("coreDatabase", properties);

        return entityManagerFactoryCore;
    }
}
