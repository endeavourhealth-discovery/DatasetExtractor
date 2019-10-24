package org.endeavourhealth.mysqlexporter.resources;

import org.endeavourhealth.mysqlexporter.repository.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LHSSQLPatient {
	public String Run(Repository repository)  throws SQLException
	{
		String result="";

		List<Integer> ids = repository.getRows("Patient","filteredPatientsDelta");

		Integer id = 0; Integer j = 0;

		while (ids.size() > j) {
			id = ids.get(j);

			result = repository.getPatientRS(id);

			System.out.println(result);
			j++;
		}

		return "stuff";
	}
}