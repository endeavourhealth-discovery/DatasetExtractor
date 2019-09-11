package org.endeavourhealth.fihrexporter.repository;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class Repository {

    private MysqlDataSource dataSource;

    private Connection connection;

    public Repository(Properties properties) throws SQLException {
        init( properties );
    }

    public boolean PreviouslyPostedCode(String code, String resource) throws SQLException {
        String q = "SELECT * FROM data_extracts.references WHERE strid='" + code + "' AND resource='" + resource +" '";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            return true;
        }
        return false;
    }

    public boolean PreviouslyPostedId(Integer id, String resource) throws SQLException {

        String q = "SELECT * FROM data_extracts.references WHERE an_id='" + id.toString() + "' AND resource='" + resource + " '";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            return true;
        }
        return false;
    }

    public String getLocation(Integer anid, String resource) throws SQLException {
        String location = "";

        String q = "SELECT * FROM data_extracts.references WHERE an_id='" + anid + "' AND resource='" + resource + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) { location =  rs.getString("location"); }

        return location;
    }

    public String GetMedicationReference(String snomedcode) throws SQLException {
        String location = "";

        String q = "SELECT * FROM data_extracts.references WHERE strid='" + snomedcode + "' AND resource='Medication'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) { location =  rs.getString("location"); }

        return location;
    }

    public boolean Audit(Integer anId, String strid, String resource, Integer responseCode, String location, String encoded, Integer patientid, Integer typeid)  throws SQLException {

        String q = "insert into data_extracts.references (an_id,strid,resource,response,location,datesent,json,patient_id,type_id) values(?,?,?,?,?,?,?,?,?)";

        PreparedStatement preparedStmt = connection.prepareStatement(q);

        preparedStmt.setInt(1, anId);
        preparedStmt.setString(2, strid);

        preparedStmt.setString(3, resource);

        preparedStmt.setString(4, responseCode.toString());
        preparedStmt.setString(5, location);

        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        preparedStmt.setTimestamp(6, ts);

        preparedStmt.setString(7, encoded);

        preparedStmt.setInt(8, patientid);

        preparedStmt.setInt(9, typeid);

        preparedStmt.execute();

        return true;
    }

    public ResultSet getOrganizationRS(Integer organization_id) throws SQLException {
            String q = "SELECT * FROM subscriber_pi.organization where id = '" + organization_id + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(q);

            ResultSet rs = preparedStatement.executeQuery();
            return rs;
    }

    public String GetTelecom(Integer patientid) throws SQLException {
        String telecom ="";


        String q = "select pc.value, cctype.name as contact_type, ccuse.name as contact_use ";
        q = q + "from subscriber_pi.patient_contact pc " + "left outer join subscriber_pi.concept ccuse on ccuse.dbid = pc.use_concept_id "
                + "left outer join subscriber_pi.concept cctype on cctype.dbid = pc.type_concept_id where pc.patient_id = '"+patientid.toString()+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        while(rs.next()) {
            if (rs.getString(3) != null) {
                telecom = telecom + rs.getString(1) + "~" + rs.getString(2) + "~" + rs.getString(3) + "|";
            }
        }

        return telecom;
    }

    public ResultSet getMedicationStatementRS(Integer record_id) throws SQLException {
        String q = "";

        q = "select " + "ms.id," + "ms.patient_id," + "ms.dose," + "ms.quantity_value," + "ms.quantity_unit," + "ms.clinical_effective_date,"
                + "c.name as medication_name," + "c.code as snomed_code, c.name as drugname "
                + "from subscriber_pi.medication_statement ms "
                + "join subscriber_pi.concept_map cm on cm.legacy = ms.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "where ms.id = '" + record_id + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        return rs;
    }

    public ResultSet getAllergyIntoleranceRS(Integer record_id) throws SQLException {
        String q = "select ";
        q =q + "ai.id,"
                + "ai.patient_id,"
                + "ai.clinical_effective_date,"
                + "c.name as allergy_name,"
                + "c.code as snomed_code "
                + "from subscriber_pi.allergy_intolerance ai "
                + "join subscriber_pi.concept_map cm on cm.legacy = ai.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "where ai.id = '"+record_id+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        return rs;
    }

    public ResultSet getPatientRS(Integer patient_id) throws SQLException {

        String q = "select distinct ";

        q = q + "p.id as patient_id,\r\n"
                + "p.nhs_number,\r\n"
                + "p.title,\r\n"
                + "p.first_names,\r\n"
                + "p.last_name,\r\n"
                + "gc.name as gender,\r\n"
                + "p.date_of_birth,\r\n"
                + "p.date_of_death,\r\n"
                + "pa.address_line_1,\r\n"
                + "pa.address_line_2,\r\n"
                + "pa.address_line_3,\r\n"
                + "pa.address_line_4,\r\n"
                + "pa.city,\r\n"
                + "pa.start_date,\r\n"
                + "pa.end_date,\r\n"
                + "cctype.name as contact_type,\r\n"
                + "ccuse.name as contact_use,\r\n"
                + "pc.value as contact_value,\r\n"
                + "p.organization_id,\r\n"
                + "org.ods_code,\r\n"
                + "org.name as org_name,\r\n"
                + "org.postcode as org_postcode\r\n "
                + "from subscriber_pi.patient p \r\n"
                + "left outer join subscriber_pi.patient_address pa on pa.id = p.current_address_id \r\n"
                + "left outer join subscriber_pi.patient_contact pc on pc.patient_id = p.id \r\n"
                + "left outer join subscriber_pi.concept ccuse on ccuse.dbid = pc.use_concept_id \r\n"
                + "left outer join subscriber_pi.concept cctype on cctype.dbid = pc.type_concept_id \r\n" + "left outer join subscriber_pi.concept gc on gc.dbid = p.gender_concept_id \r\n"
                + "left outer join subscriber_pi.organization org on org.id = p.organization_id \r\n"
                + "join subscriber_pi.observation o on o.patient_id = p.id \r\n"
                + "join subscriber_pi.concept_map cm on cm.legacy = o.non_core_concept_id \r\n"
                + "join subscriber_pi.concept c on c.dbid = cm.core \r\n"
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code \r\n"
                + "where scs.codeSetId = 1 and p.id ='" + patient_id.toString() + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        System.out.println(q);

        return rs;
    }

    public List<Integer> getRows(String table) throws SQLException {
        String preparedSql = "select * from " + table;

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );
        ResultSet rs = preparedStatement.executeQuery();
        List<Integer> result = new ArrayList<>();
        Integer id = 0;

        while (rs.next()) {
            id = rs.getInt("id");
            List<Integer> row = new ArrayList<>();
            result.add(id);
        }

        preparedStatement.close();
        return result;
    }
    public List<Integer> getPatientRows() throws SQLException {

        String preparedSql = "select * from data_extracts.cohort";

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );

        ResultSet rs = preparedStatement.executeQuery();

        List<Integer> result = new ArrayList<>();

        Integer patient_id = 0;

        while (rs.next()) {

            patient_id = rs.getInt("patient_id");

            List<Integer> row = new ArrayList<>();

            result.add(patient_id);
        }

        preparedStatement.close();

        return result;
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

        // connection.setReadOnly(true);

    }


    public void close() throws SQLException {
        connection.close();
    }
}
