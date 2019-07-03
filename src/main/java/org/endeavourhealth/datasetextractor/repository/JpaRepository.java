package org.endeavourhealth.datasetextractor.repository;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.datasetextractor.beans.Delta;
import org.endeavourhealth.datasetextractor.beans.DeltaType;
import org.endeavourhealth.datasetextractor.model.Report;

import javax.persistence.*;
import javax.xml.soap.Detail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JpaRepository {

    private EntityManagerFactory entityManagerFactory;
    private EntityManagerFactory entityManagerFactoryCore;

    public JpaRepository(Properties properties) throws SQLException {

        init( properties );
    }

    private void init(Properties props) {

        props.put("javax.persistence.jdbc.password", props.get("db.password"));
        props.put("javax.persistence.jdbc.user", props.getProperty("db.user"));
		props.put("javax.persistence.jdbc.url", props.getProperty("db.url"));

        entityManagerFactory = Persistence.createEntityManagerFactory("reportGenerator", props);

        entityManagerFactoryCore = Persistence.createEntityManagerFactory("coreDatabase", props);
    }


    public void call(String storedProceduresName) {

        log.info("Calling stored procedure {}", storedProceduresName);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(storedProceduresName);

        query.execute();

        entityManager.close();
    }

    public void close() throws SQLException {

        entityManagerFactory.close();
    }

    public List<String> getPseudoIds(Integer offset) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Query query = entityManager.createNativeQuery("select pseudo_id from dataset_wf");

        query.setFirstResult(offset);

        query.setMaxResults(1000);

        return query.getResultList();
    }


    public List<Object[]> deanonymise(List<String> pseudoIds) {

        EntityManager entityManager = entityManagerFactoryCore.createEntityManager();

        entityManager.getTransaction().begin();

        Query query = entityManager.createNativeQuery("select s.pseudo_id," +
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
                " where s.pseudo_id in (:names)");

        query.setParameter("names", pseudoIds);

        List<Object[]> rows = query.getResultList();

        Query update = entityManager.createNativeQuery("update dataset_wf d set " +
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
        }

        entityManager.getTransaction().commit();
        entityManager.close();

        return rows;
    }

	public List<Delta> getAlterations(Report report) {

    	return null;
	}

	public List<Delta> getAdditions(Report report) {

        List<Delta> deltas = new ArrayList<>();

		EntityManager entityManager = entityManagerFactory.createEntityManager();

		String sql = "select t.* from " + report.getDatasetTable() + " t left join " + report.getDatasetTableYesterday() + " y " +
		"on t.id = y.id and t.hash = y.hash " +
		"where y.id is null";

		Query query = entityManager.createNativeQuery( sql );

		List<Object[]> rows = query.getResultList();

		for(Object[] data : rows) {
		    Delta delta = new Delta();
		    delta.populateRow(data);
		    delta.setType(DeltaType.ADDITION);

            deltas.add(delta);
        }
    	return deltas;
	}

	public void renameTable(Report report) {

		log.info("Renaming table {} to {} ", report.getDatasetTable(), report.getDatasetTableYesterday());

    	String sql = "RENAME TABLE " + report.getDatasetTable() + " TO " + report.getDatasetTableYesterday();

    	EntityManager entityManager = entityManagerFactory.createEntityManager();

		entityManager.getTransaction().begin();

    	Query query = entityManager.createNativeQuery( sql );

    	query.executeUpdate();

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
