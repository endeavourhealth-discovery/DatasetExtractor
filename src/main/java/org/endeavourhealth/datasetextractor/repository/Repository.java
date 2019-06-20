package org.endeavourhealth.datasetextractor.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Repository {

    private MysqlDataSource dataSource;

    private Connection connection;

    private String tableName;

    private String[] headers;
    private int columnCount;
    private String sql;

    public Repository(Properties properties) throws SQLException {
        init( properties );

        initTableHeaders();
    }


    public String[] getHeaders() throws SQLException {
        return headers;
    }

    private void initTableHeaders() throws SQLException {

        String preparedSql = sql + " limit 1";

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );

        ResultSet rs = preparedStatement.executeQuery();

        ResultSetMetaData metaData = rs.getMetaData();

        columnCount = metaData.getColumnCount();

        String[] labels = null;
        if (metaData != null) {
            labels = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                labels[i] = metaData.getColumnLabel(i + 1);
            }
        }

        headers = labels;
    }

    public List<List<String>> getRows(int offset, int pageSize) throws SQLException {

        String preparedSql = sql + " limit " + offset + ", " + pageSize;

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );

        ResultSet rs = preparedStatement.executeQuery();

        List<List<String>> result = new ArrayList<>();

        while (rs.next()) {

            List<String> row = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                row.add( rs.getString( i ));
            }

            result.add( row );
        }

        return result;
    }

    private String whitelist(String tableName) {
        return tableName.replaceAll("/[^0-9a-zA-Z_]/", "");
    }


    private void init(Properties props) throws SQLException {

        dataSource = new MysqlDataSource();

        dataSource.setURL(props.getProperty("url"));
        dataSource.setUser(props.getProperty("user"));
        dataSource.setPassword(props.getProperty("password"));

        dataSource.setReadOnlyPropagatesToServer(true);

        connection = dataSource.getConnection();

        connection.setReadOnly( true );

        String tableName = props.getProperty( "tablename" );
        String orderBy = props.getProperty( "orderBy" );

        log.info("Using table name {}", tableName);
        log.info("Using order by {}", orderBy);

        this.tableName = whitelist(tableName);

        this.sql =  "select * from " + tableName + " order by " + orderBy;

        log.info("Sanitised table name {}", this.tableName);
    }

    public void close() throws SQLException {

        connection.close();
    }
}
