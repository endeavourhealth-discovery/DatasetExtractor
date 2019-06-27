package org.endeavourhealth.datasetextractor.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
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

         entityManagerFactory = Persistence.createEntityManagerFactory("reportGenerator");

        entityManagerFactoryCore = Persistence.createEntityManagerFactory("coreDatabase");
    }


    public void call(String storedProceduresName) {

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

        Query query = entityManager.createNativeQuery("select psuedo_id from dataset_wf");

        query.setFirstResult(offset);

    query.setMaxResults(1000);

        return query.getResultList();




    }


    public List<Object[]> deanonymise(List<String> pseudoIds) {

        EntityManager entityManager = entityManagerFactoryCore.createEntityManager();

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

        List rows = query.getResultList();

        entityManager.close();

        return rows;
    }
}
