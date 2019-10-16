package org.endeavourhealth.mysqlexporter.resources;

import org.endeavourhealth.mysqlexporter.repository.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LHSSQLAllergyIntolerance {
	public String Run(Repository repository)  throws SQLException
	{
		String result="";

		List<Integer> ids = repository.getRows("AllergyIntolerance","filteredAllergiesDelta");

		Integer id = 0; Integer j = 0;

		while (ids.size() > j) {
			id = ids.get(j);

			result = repository.getAllergyIntoleranceRS(id);

			System.out.println(result);
			j++;
		}

		return "stuff";
	}
}