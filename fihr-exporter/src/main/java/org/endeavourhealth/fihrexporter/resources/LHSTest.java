package org.endeavourhealth.fihrexporter.resources;
import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.repository.Repository;
import org.hl7.fhir.dstu3.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LHSTest {
	public void Run(Repository repository) throws SQLException {
		String result="";

		Integer nor = 0;
		String snomedcode = ""; String drugname = "";

		String dose = ""; String quantityvalue; String quantityunit = "";
		String clinicaleffdate = ""; String location = ""; Integer typeid = 10;
		Integer id = 0;

		result = repository.getMedicationStatementRS(14189472);

		if (result.length()>0) {

			System.out.println(result);

			String[] ss = result.split("\\`");
			nor = Integer.parseInt(ss[0]);
			snomedcode = ss[1];
			drugname = ss[2];

			boolean prev = repository.PreviouslyPostedId(nor, "Patient");

			prev = repository.PreviouslyPostedCode(snomedcode,"Medication");

			location = repository.getLocation(nor, "Patient");

			String rxref = repository.GetMedicationReference(snomedcode);

			dose=ss[3]; quantityvalue=ss[4]; quantityunit=ss[5]; clinicaleffdate=ss[6]; id= Integer.parseInt(ss[7]);

		}

	}
}