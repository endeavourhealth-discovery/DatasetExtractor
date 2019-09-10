package org.endeavourhealth.fihrexporter.repository;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Repository {

    private MysqlDataSource dataSource;

    private Connection connection;

    public Repository(Properties properties) throws SQLException {
        init( properties );
    }

    public List<List<String>> getRows(int offset, int pageSize) throws SQLException {

        String preparedSql = "select * from table";

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );

        ResultSet rs = preparedStatement.executeQuery();

        List<List<String>> result = new ArrayList<>();

        while (rs.next()) {

        }

        preparedStatement.close();

        return result;
    }

    private void init(Properties props) throws SQLException {

        dataSource = new MysqlDataSource();

        dataSource.setURL(props.getProperty("url"));
        dataSource.setUser(props.getProperty("user"));
        dataSource.setPassword(props.getProperty("password"));

        dataSource.setReadOnlyPropagatesToServer(true);

        connection = dataSource.getConnection();

        connection.setReadOnly(true);

    }


    public void close() throws SQLException {
        connection.close();
    }
}
