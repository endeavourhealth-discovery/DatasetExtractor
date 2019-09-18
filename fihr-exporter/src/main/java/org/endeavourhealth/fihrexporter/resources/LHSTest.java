package org.endeavourhealth.fihrexporter.resources;
import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.repository.Repository;
import org.hl7.fhir.dstu3.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LHSTest {
	public void Run(Repository repository) throws SQLException {
		ResultSet rs;

		Integer nor =0; // patientid
		String snomedcode =""; String orginalterm=""; String result_value="";
		String clineffdate = ""; String resultvalunits = ""; String location="";
		Integer typeid = 11; String t = ""; Integer parent =0; String parentids = "";

		/*
		rs = repository.getObservationRS(28827);
		if (rs.next()) {
			nor = rs.getInt(2);
			snomedcode = rs.getString(3);
			orginalterm = rs.getString(4);
			result_value = rs.getString(5);
			clineffdate = rs.getString(6);
			resultvalunits = rs.getString(7);

			parent = rs.getInt("parent_observation_id");
		}
		 */
	}
}