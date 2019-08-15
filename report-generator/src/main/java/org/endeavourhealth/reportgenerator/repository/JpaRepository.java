package org.endeavourhealth.reportgenerator.repository;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.beans.DeltaType;
import org.endeavourhealth.reportgenerator.model.Database;
import org.endeavourhealth.reportgenerator.model.Report;

import javax.persistence.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JpaRepository {

    private EntityManagerFactory entityManagerFactoryCompass;
    private EntityManagerFactory entityManagerFactoryCore;

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
            case PCR:
                properties.put("javax.persistence.jdbc.password", properties.get("db.pcr.password"));
                properties.put("javax.persistence.jdbc.user", properties.getProperty("db.pcr.user"));
                properties.put("javax.persistence.jdbc.url", properties.getProperty("db.pcr.url"));
                break;
        }

        entityManagerFactoryCompass = Persistence.createEntityManagerFactory("compassDatabase", properties);
    }


    public void call(String storedProceduresName, Report report) {

        log.info("Calling stored procedure {} with database {}", storedProceduresName, report.getStoredProcedureDatabase());

        EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(storedProceduresName);

        query.execute();

        entityManager.close();
    }

    public void close() throws SQLException {

        entityManagerFactoryCompass.close();
        entityManagerFactoryCore.close();
    }

    public List<String> getPseudoIds(Integer offset) {
        EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

        String sql = "select distinct pseudo_id from dataset_wf limit " + offset + ", 1000";
        Query query = entityManager.createNativeQuery(sql);

        log.trace("Sql {}", sql);

        return query.getResultList();
    }


    public List<Object[]> deanonymise(List<String> pseudoIds) {

        EntityManager entityManagerCore = entityManagerFactoryCore.createEntityManager();
        EntityManager entityManagerCompass = entityManagerFactoryCompass.createEntityManager();

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

            update.setParameter(11, row[0]);

            update.executeUpdate();

            log.trace("Updating {}", row[0]);
        }

        entityManagerCore.getTransaction().commit();
        entityManagerCore.close();

        entityManagerCompass.getTransaction().commit();
        entityManagerCompass.close();

        return rows;
    }
}
