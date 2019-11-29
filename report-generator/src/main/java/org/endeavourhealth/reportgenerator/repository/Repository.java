package org.endeavourhealth.reportgenerator.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.endeavourhealth.reportgenerator.model.Report;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class Repository {

    protected EntityManagerFactory getEntityManagerFactory(String simpleName,  List<String> managedClassNames, Properties dbProperties) {

        PersistenceUnitInfo persistenceUnitInfo =  getPersistenceUnitInfo(getClass().getSimpleName(), managedClassNames, dbProperties);

        Map<String, Object> configuration = new HashMap<>();

        return new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo), configuration).build();
    }

    private HibernatePersistenceUnitInfo getPersistenceUnitInfo(String name, List<String> managedClassNames,Properties dbProperties) {
        return new HibernatePersistenceUnitInfo(name, managedClassNames, getProperties( dbProperties ));
    }

    private DataSource getMysqlDataSource(Properties dbProperties) {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL( dbProperties.getProperty("javax.persistence.jdbc.url") );
        mysqlDataSource.setUser( dbProperties.getProperty("javax.persistence.jdbc.user") );
        mysqlDataSource.setPassword( dbProperties.getProperty("javax.persistence.jdbc.password") );
        return mysqlDataSource;
    }

    private Properties getProperties(Properties dbProperties) {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("packagesToScan", "org.endeavourhealth.reportgenerator.model");
        properties.put("hibernate.connection.datasource", getMysqlDataSource( dbProperties ));
        return properties;
    }
}
