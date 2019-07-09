package org.endeavourhealth.reportgenerator.repository;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.beans.Delta;
import org.endeavourhealth.reportgenerator.beans.DeltaType;
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

    public JpaRepository(Properties properties) throws SQLException {

        init( properties );
    }

    private void init(Properties props) {

        props.put("javax.persistence.jdbc.password", props.get("db.compass.password"));
        props.put("javax.persistence.jdbc.user", props.getProperty("db.compass.user"));
		    props.put("javax.persistence.jdbc.url", props.getProperty("db.compass.url"));

        entityManagerFactoryCompass = Persistence.createEntityManagerFactory("compassDatabase", props);

        props.put("javax.persistence.jdbc.password", props.get("db.core.password"));
        props.put("javax.persistence.jdbc.user", props.getProperty("db.core.user"));
        props.put("javax.persistence.jdbc.url", props.getProperty("db.core.url"));

        entityManagerFactoryCore = Persistence.createEntityManagerFactory("coreDatabase", props);
    }


    public void call(String storedProceduresName) {

        log.info("Calling stored procedure {}", storedProceduresName);

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

        log.debug("Sql {}", sql);
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

    public List<Delta> getDeletions(Report report) {
        List<Delta> deltas = new ArrayList<>();

        EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

        String sql = "select y.* from " + report.getDatasetTable() + " t right join " + report.getDatasetTableYesterday() + " y " +
                "on t.id = y.id where t.id is null";

        Query query = entityManager.createNativeQuery( sql );

        List<Object[]> rows = query.getResultList();

        for(Object[] data : rows) {
            Delta delta = new Delta();
            delta.populateRow(data);
            delta.setType(DeltaType.DELETION);

            deltas.add(delta);
        }
        return deltas;
    }

	public List<Delta> getAlterations(Report report) {
        List<Delta> deltas = new ArrayList<>();

        EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

        String sql = "select t.* from " + report.getDatasetTable() + " t join " + report.getDatasetTableYesterday() + " y " +
                "on t.id = y.id and t.hash != y.hash ";

        Query query = entityManager.createNativeQuery( sql );

        List<Object[]> rows = query.getResultList();

        for(Object[] data : rows) {
            Delta delta = new Delta();
            delta.populateRow(data);
            delta.setType(DeltaType.ALTERATION);

            deltas.add(delta);
        }
        return deltas;
	}

	public List<Delta> getAdditions(Report report) {

        List<Delta> deltas = new ArrayList<>();

		EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

		String sql = "select t.* from " + report.getDatasetTable() + " t left join " + report.getDatasetTableYesterday() + " y " +
		"on t.id = y.id " +
		"where y.id is null";

		Query query = entityManager.createNativeQuery( sql );

		List<Object[]> rows = query.getResultList();

		for(Object[] data : rows) {
		    Delta delta = new Delta();
		    delta.populateRow(data);
		    delta.setType(DeltaType.ADDITION);

            deltas.add(delta);
        }

        log.debug("Have found {} addition rows", deltas.size());

    	return deltas;
	}

	public void renameTable(Report report) {

		log.info("Renaming table {} to {} ", report.getDatasetTable(), report.getDatasetTableYesterday());

    	EntityManager entityManager = entityManagerFactoryCompass.createEntityManager();

		entityManager.getTransaction().begin();

        Query query = entityManager.createNativeQuery( "drop table if exists " + report.getDatasetTableYesterday() );

        query.executeUpdate();

    	query = entityManager.createNativeQuery( "RENAME TABLE " + report.getDatasetTable() + " TO " + report.getDatasetTableYesterday() );

    	query.executeUpdate();

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
