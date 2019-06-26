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

    public JpaRepository(Properties properties) throws SQLException {

        init( properties );
    }

    private void init(Properties props) {

         entityManagerFactory = Persistence.createEntityManagerFactory("reportGenerator");
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
}
