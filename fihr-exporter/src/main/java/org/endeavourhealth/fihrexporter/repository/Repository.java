package org.endeavourhealth.fihrexporter.repository;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.endeavourhealth.common.config.ConfigManager;

import java.sql.*;
import java.util.*;

public class Repository {

    private MysqlDataSource dataSource;

    private Connection connection;

    private String baseURL;

    public String outputFHIR;
    public String dbschema;
    public String clientid;
    public String clientsecret;
    public String scope;
    public String granttype;
    public String tokenurl;
    public String token;
    public String runguid;
    public Integer scaletotal;

    public Repository(Properties properties) throws SQLException {
        init( properties );
    }

    public boolean PreviouslyPostedCode(String code, String resource) throws SQLException {
        String q = "SELECT * FROM data_extracts.references WHERE strid='" + code + "' AND resource='" + resource +" '";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            preparedStatement.close();
            return true;
        }
        preparedStatement.close();
        return false;
    }

    public boolean PreviouslyPostedId(Integer id, String resource) throws SQLException {

        String q = "SELECT * FROM data_extracts.references WHERE an_id='" + id.toString() + "' AND resource='" + resource + " '";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            preparedStatement.close();
            return true;
        }

        preparedStatement.close();

        return false;
    }

    public String getLocation(Integer anid, String resource) throws SQLException {
        String location = "";

        String q = "SELECT * FROM data_extracts.references WHERE an_id='" + anid + "' AND resource='" + resource + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) { location =  rs.getString("location"); }

        preparedStatement.close();

        // Has the resource been deleted?
        if (location.length()>0) {
            q = "SELECT * FROM data_extracts.references WHERE an_id='" + anid + "' AND resource='DEL:" + resource + "'";
            preparedStatement = connection.prepareStatement(q);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                location = "";
            }
            preparedStatement.close();
        }

        return location;
    }

    public String GetMedicationReference(String snomedcode) throws SQLException {
        String location = "";

        String q = "SELECT * FROM data_extracts.references WHERE strid='" + snomedcode + "' AND resource='Medication'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) { location =  rs.getString("location"); }

        preparedStatement.close();
        return location;
    }

    public void DeleteTracker() throws SQLException
    {
        String q ="DELETE FROM data_extracts.references where resource ='Tracker'";

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.execute();

        preparedStmt.close();
    }

    public void DeleteFileReferences() throws SQLException
    {
        String q ="DELETE FROM data_extracts.references where response = 123";

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.execute();
    }

    private void PurgetheQueue(Integer anId, String resource) throws SQLException
    {
        // purge the queues
        String table = ""; String q = "";

        if (resource=="Patient") {table="data_extracts.filteredpatientsdelta";}
        if (resource=="Observation") {table="data_extracts.filteredobservationsdelta";}
        if (resource=="MedicationStatement") {table="data_extracts.filteredmedicationsdelta";}
        if (resource== "AllergyIntolerance") {table="data_extracts.filteredallergiesdelta";};

        if (table.length()>0) {
            q = "DELETE FROM " + table + " where id='" + anId + "'";
            //PreparedStatement preparedStatement = connection.prepareStatement(q);
            //ResultSet rs = preparedStatement.executeQuery();
        }
    }

    public boolean UpdateAudit(Integer anId, String strid, String encoded, Integer responseCode, String resource) throws SQLException
    {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        String str = ts.toString();

        String q = "";

        if (anId != 0) {
            q = "update data_extracts.references set response = " + responseCode + ", datesent = '"+str+"', json = '"+encoded+"' where an_id = '"+anId+"'";
            PurgetheQueue(anId, resource);
        }

        if (strid.length() > 0) {
            q = "update data_extracts.references set response = " + responseCode + ", datesent = '" + str + "', json = '" + encoded + "' where strid = '"+strid+"'";
        }

        PreparedStatement preparedStmt = connection.prepareStatement(q);
        preparedStmt.execute();

        preparedStmt.close();

        return true;
    }

    public boolean Audit(Integer anId, String strid, String resource, Integer responseCode, String location, String encoded, Integer patientid, Integer typeid) throws SQLException
    {

        String q = "insert into data_extracts.references (an_id,strid,resource,response,location,datesent,json,patient_id,type_id,runguid) values(?,?,?,?,?,?,?,?,?,?)";

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

        preparedStmt.setString(10, this.runguid);

        preparedStmt.execute();

        preparedStmt.close();

        if (anId != 0) {PurgetheQueue(anId, resource);}

        return true;
    }

    public String getOrganizationRS(Integer organization_id) throws SQLException {

        String result = "";
        //String q = "SELECT * FROM subscriber_pi.organization where id = '" + organization_id + "'";
        String q = "SELECT * FROM "+dbschema+".organization where id = '" + organization_id + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            String odscode = rs.getString("ods_code");
            String name = rs.getString("name");
            String postcode = rs.getString("postcode");
            Integer id = rs.getInt("id");

            result = odscode + "~" + name + "~" + postcode + "~" + id;
        }

        preparedStatement.close();

        return result;

    }

    public String GetTelecom(Integer patientid) throws SQLException {
        String telecom ="";

        //String q = "select pc.value, cctype.name as contact_type, ccuse.name as contact_use ";
        //q = q + "from subscriber_pi.patient_contact pc " + "left outer join subscriber_pi.concept ccuse on ccuse.dbid = pc.use_concept_id "
        //        + "left outer join subscriber_pi.concept cctype on cctype.dbid = pc.type_concept_id where pc.patient_id = '"+patientid.toString()+"'";

        String q = "select pc.value, cctype.name as contact_type, ccuse.name as contact_use ";
        q = q + "from "+dbschema+".patient_contact pc " + "left outer join "+dbschema+".concept ccuse on ccuse.dbid = pc.use_concept_id "
                + "left outer join "+dbschema+".concept cctype on cctype.dbid = pc.type_concept_id where pc.patient_id = '"+patientid.toString()+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        while(rs.next()) {
            if (rs.getString(3) != null) {
                telecom = telecom + rs.getString(1) + "`" + rs.getString(2) + "`" + rs.getString(3) + "|";
            }
        }

        preparedStatement.close();

        return telecom;
    }

    public String getMedicationStatementRS(Integer record_id) throws SQLException {
        String q = ""; String result = "";

        /*
        q = "select " + "ms.id," + "ms.patient_id," + "ms.dose," + "ms.quantity_value," + "ms.quantity_unit," + "ms.clinical_effective_date,"
                + "c.name as medication_name," + "c.code as snomed_code, c.name as drugname "
                + "from subscriber_pi.medication_statement ms "
                + "join subscriber_pi.concept_map cm on cm.legacy = ms.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "where ms.id = '" + record_id + "'";
         */

        q = "select " + "ms.id," + "ms.patient_id," + "ms.dose," + "ms.quantity_value," + "ms.quantity_unit," + "ms.clinical_effective_date,"
                + "c.name as medication_name," + "c.code as snomed_code, c.name as drugname "
                + "from "+dbschema+".medication_statement ms "
                // + "join "+dbschema+".concept_map cm on cm.legacy = ms.non_core_concept_id "
                // + "join "+dbschema+".concept c on c.dbid = cm.core "
                + "join "+dbschema+".concept c on c.dbid = ms.non_core_concept_id "
                + "where ms.id = '" + record_id + "'";

        //System.out.println(q);
        //Scanner scan = new Scanner(System.in);
        //System.out.print("Press any key to continue . . . ");
        //scan.nextLine();

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            Integer nor = rs.getInt("patient_id");
            String snomedcode = rs.getString("snomed_code");
            String drugname = rs.getString("drugname");
            String dose = rs.getString("dose"); String quantityvalue = rs.getString("quantity_value");
            String quantityunit = rs.getString("quantity_unit"); String clinicaleffdate = rs.getString("clinical_effective_date");
            Integer id = rs.getInt(1);

            if (rs.getString("dose")==null) {dose="";}
            if (rs.getString("quantity_value")==null) {quantityvalue="";}
            if (rs.getString("quantity_unit")==null) {quantityunit="";}

            // dose contained a ~!
            result = nor+"`"+snomedcode+"`"+drugname+"`"+dose+"`"+quantityvalue+"`"+quantityunit+"`"+clinicaleffdate+"`"+id;
        }
        preparedStatement.close();

        return result;
    }

    public String getObservationRecord(String id) throws SQLException {

        String obsrec = ""; String snomedcode = ""; String orginalterm = "";
        String result_value = ""; String clineffdate = ""; String resultvalunits = "";

        Integer noncoreconceptid = 0;

        /*
        String q = "select ";
        q = q + "o.id,"
                + "o.patient_id,"
                + "c.code as snomed_code,"
                + "c.name as original_term,"
                + "o.result_value,"
                + "o.clinical_effective_date,"
                + "o.parent_observation_id,"
                + "o.result_value_units,"
                + "o.non_core_concept_id "
                + "from subscriber_pi.observation o "
                + "join subscriber_pi.concept_map cm on cm.legacy = o.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code "
                + "where o.id = '"+id+"'";
         */

        String q = "select ";
        q = q + "o.id,"
                + "o.patient_id,"
                + "c.code as snomed_code,"
                + "c.name as original_term,"
                + "o.result_value,"
                + "o.clinical_effective_date,"
                + "o.parent_observation_id,"
                + "o.result_value_units,"
                + "o.non_core_concept_id "
                + "from "+dbschema+".observation o "
                + "join "+dbschema+".concept_map cm on cm.legacy = o.non_core_concept_id "
                + "join "+dbschema+".concept c on c.dbid = cm.core "
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code "
                + "where o.id = '"+id+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            snomedcode = rs.getString(3); orginalterm = rs.getString(4);
            result_value = rs.getString(5); clineffdate = rs.getString(6); resultvalunits = rs.getString(8);
            noncoreconceptid = rs.getInt("non_core_concept_id");
            obsrec = snomedcode + "~" + orginalterm + "~" + result_value + "~" + clineffdate + "~" + resultvalunits + "~" + noncoreconceptid;
        }

        preparedStatement.close();

        if (obsrec.length()==0) {
            //q = "select * from subscriber_pi.observation where id = "+id;
            q = "select * from "+dbschema+".observation where id = "+id;
            preparedStatement = connection.prepareStatement(q);
            rs = preparedStatement.executeQuery();
            if (rs.next()) { ;
                result_value = rs.getString("result_value"); clineffdate = rs.getString("clinical_effective_date"); resultvalunits = rs.getString("result_value_units");
                noncoreconceptid = rs.getInt("non_core_concept_id");
                obsrec = "~~"+result_value+"~"+clineffdate+"~"+resultvalunits+"~"+noncoreconceptid;
            }
            preparedStatement.close();
        }

        //preparedStatement.close();

        return obsrec;
    }

    public String getIdsFromParent(Integer parentid) throws SQLException {
        String ids = "";

        //String q = "SELECT id FROM subscriber_pi.observation WHERE parent_observation_id="+parentid;
        String q = "SELECT id FROM "+dbschema+".observation WHERE parent_observation_id="+parentid;

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        while(rs.next()) {
            ids = ids + rs.getString(1) + "~";
        }

        preparedStatement.close();

        return ids;
    }

    public String getObservationRS(Integer record_id) throws SQLException {
        String result = "";

        /*
        String q = "select ";
        q = q + "o.id,"
                + "o.patient_id,"
                + "c.code as snomed_code,"
                + "c.name as original_term,"
                + "o.result_value,"
                + "o.clinical_effective_date,"
                + "o.parent_observation_id,"
                + "o.result_value_units "
                + "from subscriber_pi.observation o "
                + "join subscriber_pi.concept_map cm on cm.legacy = o.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code "
                + "where scs.codeSetId = 2 and o.id = '"+record_id+"'";
         */

        String q = "select ";
        q = q + "o.id,"
                + "o.patient_id,"
                + "c.code as snomed_code,"
                + "c.name as original_term,"
                + "o.result_value,"
                + "o.clinical_effective_date,"
                + "o.parent_observation_id,"
                + "o.result_value_units "
                + "from "+dbschema+".observation o "
                + "join "+dbschema+".concept_map cm on cm.legacy = o.non_core_concept_id "
                + "join "+dbschema+".concept c on c.dbid = cm.core "
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code "
                + "where scs.codeSetId = 2 and o.id = '"+record_id+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            Integer nor = rs.getInt("patient_id"); String snomedcode = rs.getString("snomed_code"); String orginalterm = rs.getString("original_term");
            String result_value = rs.getString("result_value"); String clineffdate = rs.getString("clinical_effective_date"); String resultvalunits = rs.getString("result_value_units");

            if (rs.getString("result_value") == null) {result_value="";}
            if (rs.getString("result_value_units") == null) {resultvalunits="";}

            result = nor.toString()+"~"+snomedcode+"~"+orginalterm+"~"+result_value+"~"+clineffdate+"~"+resultvalunits+"~"+rs.getInt("parent_observation_id");
        }

        preparedStatement.close();

        return result;
    }

    public String getAllergyIntoleranceRS(Integer record_id) throws SQLException {
        String q = "select "; String result = "";

        /*
        q =q + "ai.id,"
                + "ai.patient_id,"
                + "ai.clinical_effective_date,"
                + "c.name as allergy_name,"
                + "c.code as snomed_code "
                + "from subscriber_pi.allergy_intolerance ai "
                + "join subscriber_pi.concept_map cm on cm.legacy = ai.non_core_concept_id "
                + "join subscriber_pi.concept c on c.dbid = cm.core "
                + "where ai.id = '"+record_id+"'";
         */

        q =q + "ai.id,"
                + "ai.patient_id,"
                + "ai.clinical_effective_date,"
                + "c.name as allergy_name,"
                + "c.code as snomed_code "
                + "from "+dbschema+".allergy_intolerance ai "
                //+ "join "+dbschema+".concept_map cm on cm.legacy = ai.non_core_concept_id "
                //+ "join "+dbschema+".concept c on c.dbid = cm.core "
                + "join "+dbschema+".concept c on c.dbid = ai.non_core_concept_id "
                + "where ai.id = '"+record_id+"'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            Integer nor = rs.getInt("patient_id");
            String clineffdate = rs.getString(3);
            String allergyname = rs.getString(4);
            String snomedcode = rs.getString(5);
            result = nor+"~"+clineffdate+"~"+allergyname+"~"+snomedcode;
        }

        preparedStatement.close();

        if (result.length()==0) {
            System.out.println("?"+record_id);
            System.out.println(q);
        }

        return result;
    }

    public String getPatientRS(Integer patient_id) throws SQLException {

        String q = "select distinct ";

        /*
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
                + "pa.postcode,\r\n"
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
                + "left outer join subscriber_pi.concept cctype on cctype.dbid = pc.type_concept_id \r\n"
                + "left outer join subscriber_pi.concept gc on gc.dbid = p.gender_concept_id \r\n"
                + "left outer join subscriber_pi.organization org on org.id = p.organization_id \r\n"
                + "join subscriber_pi.observation o on o.patient_id = p.id \r\n"
                + "join subscriber_pi.concept_map cm on cm.legacy = o.non_core_concept_id \r\n"
                + "join subscriber_pi.concept c on c.dbid = cm.core \r\n"
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code \r\n"
                + "where scs.codeSetId = 1 and p.id ='" + patient_id.toString() + "'";
         */

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
                + "pa.postcode,\r\n"
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
                + "from "+dbschema+".patient p \r\n"
                + "left outer join "+dbschema+".patient_address pa on pa.id = p.current_address_id \r\n"
                + "left outer join "+dbschema+".patient_contact pc on pc.patient_id = p.id \r\n"
                + "left outer join "+dbschema+".concept ccuse on ccuse.dbid = pc.use_concept_id \r\n"
                + "left outer join "+dbschema+".concept cctype on cctype.dbid = pc.type_concept_id \r\n"
                + "left outer join "+dbschema+".concept gc on gc.dbid = p.gender_concept_id \r\n"
                + "left outer join "+dbschema+".organization org on org.id = p.organization_id \r\n"
                + "join "+dbschema+".observation o on o.patient_id = p.id \r\n"
                + "join "+dbschema+".concept_map cm on cm.legacy = o.non_core_concept_id \r\n"
                + "join "+dbschema+".concept c on c.dbid = cm.core \r\n"
                + "join data_extracts.snomed_code_set_codes scs on scs.snomedCode = c.code \r\n"
                + "where scs.codeSetId = 1 and p.id ='" + patient_id.toString() + "'";

        PreparedStatement preparedStatement = connection.prepareStatement(q);

        ResultSet rs = preparedStatement.executeQuery();

        String result="";
        if (rs.next()) {
            String nhsno = rs.getString("nhs_number");
            String dob = rs.getString("date_of_birth");
            String odscode = rs.getString("ods_code");
            String orgname = rs.getString("org_name");
            String orgpostcode = rs.getString("org_postcode");

            String telecom = GetTelecom(patient_id);

            String dod = rs.getString("date_of_death");

            String add1="";
            if (rs.getString("address_line_1")!=null) {add1 = rs.getString("address_line_1");}

            String add2="";
            // test
            if (rs.getString("address_line_2")!=null) add2 = rs.getString("address_line_2");

            String add3="";
            if (rs.getString("address_line_3")!=null) add3 = rs.getString("address_line_3");

            String add4="";
            if (rs.getString("address_line_4")!=null) add4 = rs.getString("address_line_4");

            String city="";
            if (rs.getString("city")!=null) city = rs.getString("city");

            String postcode="";
            if (rs.getString("postcode")!=null) postcode = rs.getString("postcode");

            String gender = rs.getString("gender");
            String contacttype = rs.getString("contact_type");
            String contactuse = rs.getString("contact_use");
            String contactvalue = rs.getString("contact_value");
            String title = rs.getString("title");
            String firstname = rs.getString("first_names");
            String lastname = rs.getString("last_name");

            String startdate = rs.getString("start_date"); // date added to the cohort?
            Integer orgid = rs.getInt("organization_id");
            ;

            result = nhsno + "~" + odscode + "~" + orgname + "~" + orgpostcode + "~" + telecom + "~" + dod + "~" + add1 + "~" + add2 + "~" + add3 + "~" + add4 + "~" + city + "~";
            result = result + gender + "~" + contacttype + "~" + contactuse + "~" + contactvalue + "~" + title + "~" + firstname + "~" + lastname + "~" + startdate + "~" + orgid + "~" + dob + "~" + postcode + "~";
        }

        preparedStatement.close();

        return result;
    }

    public List<Integer> getRows(String table) throws SQLException {
        String preparedSql = "select * from " + table;

        // preparedSql = preparedSql + " where id>14189471 order by id asc";

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );
        ResultSet rs = preparedStatement.executeQuery();
        List<Integer> result = new ArrayList<>();
        Integer id = 0; Integer count = 0;

        while (rs.next()) {
            id = rs.getInt("id");

            List<Integer> row = new ArrayList<>();
            result.add(id);

            count=count+1;
            if (count > this.scaletotal) break;
        }
        preparedStatement.close();

        //List<Integer> result = new ArrayList<>();
        //result.add(56229);

        return result;
    }
    public List<Integer> getPatientRows() throws SQLException {

        // String preparedSql = "select * from data_extracts.cohort";
        String preparedSql = "select * from data_extracts.filteredPatientsDelta";

        PreparedStatement preparedStatement = connection.prepareStatement( preparedSql );

        ResultSet rs = preparedStatement.executeQuery();

        List<Integer> result = new ArrayList<>();

        Integer patient_id = 0; Integer count = 0;

        while (rs.next()) {

            //patient_id = rs.getInt("patient_id");
            patient_id = rs.getInt("id");

            List<Integer> row = new ArrayList<>();

            result.add(patient_id);

            if (count > this.scaletotal) break;
        }

        preparedStatement.close();


        // Testing
        //List<Integer> result = new ArrayList<>();
        //result.add(28844);

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

    public String getBaseURL()
    {
        return baseURL;
    }

    public String getConfig()
    {
        String conStr = ConfigManager.getConfiguration("database","knowdiabetes");
        //String conStr = ConfigManager.getConfiguration("global","slack");
        System.out.println(conStr);
        return conStr;
    }

    private void init(Properties props) throws SQLException {

        try {
            System.out.println("initializing properties");

            String conStr = getConfig();
            String[] ss = conStr.split("\\`");

            // sqlurl~username~password~clientid~clientsecret~scope~tokenurl~baseurl
            //String zsqlurl=ss[0]; String zsqlusername=ss[1]; String zsqlpass=ss[2];
            //String zclientid=ss[3]; String zclientsecret= ss[4]; String zscope=ss[5];
            //String ztokenurl =ss[6]; String zbaseurl = ss[7];

            //baseURL = props.getProperty("baseurl");
            baseURL = ss[7];
            outputFHIR = props.getProperty("outputFHIR");
            dbschema = props.getProperty("dbschema");
            //clientid = props.getProperty("clientid");
            clientid = ss[3];
            //clientsecret = props.getProperty("clientsecret");
            clientsecret = ss[4];
            //scope = props.getProperty("scope");
            scope = ss[5];
            granttype = props.getProperty("granttype");
            //tokenurl = props.getProperty("tokenurl");
            tokenurl = ss[6];
            token = props.getProperty("token");
            runguid = props.getProperty("runguid");

            scaletotal = Integer.parseInt(props.getProperty("scaletotal"));

            dataSource = new MysqlDataSource();

            System.out.println(">> " + outputFHIR);

            //dataSource.setURL(props.getProperty("url"));
            //dataSource.setUser(props.getProperty("user"));
            //dataSource.setPassword(props.getProperty("password"));

            dataSource.setURL(ss[0]);
            dataSource.setUser(ss[1]);
            dataSource.setPassword(ss[2]);

            dataSource.setReadOnlyPropagatesToServer(true);

            connection = dataSource.getConnection();

            // connection.setReadOnly(true);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }


    public void close() throws SQLException {
        connection.close();
    }
}
