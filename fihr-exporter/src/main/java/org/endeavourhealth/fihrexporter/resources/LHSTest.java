package org.endeavourhealth.fihrexporter.resources;
import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.repository.Repository;
import org.hl7.fhir.dstu3.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class LHSTest {

    private void ReconcileTables(Repository repository, String table, String resource) throws SQLException {
        // tables:
        // filteredMedicationsDelta
        // filteredAllergiesDelta
        // filteredPatientsDelta
        List<Integer> ids = repository.getRows(table);
        Integer j = 0; Integer id = 0;
        String location="";
        while (ids.size() > j) {
            id = ids.get(j);
            location = repository.getLocation(id, resource);
            if (location.length()==0) {
                System.out.println(id+" "+resource);
            }
            j++;
        }
    }

    public void ReconcileOtherTables(Repository repository)  throws SQLException {
        ReconcileTables(repository,"filteredMedicationsDelta","MedicationStatement");
        ReconcileTables(repository,"filteredAllergiesDelta","AllergyIntolerance");
        ReconcileTables(repository,"filteredPatientsDelta","Patient");
    }

    public void ReconcileObservations(Repository repository) throws SQLException {
        List<Integer> ids = repository.getRows("filteredObservationsDelta");
        Integer j = 0; Integer id = 0;
        String location="";
        while (ids.size() > j) {
            id = ids.get(j);
            location = repository.getLocation(id, "Observation");
            if (location.length()==0) {
                // is it a Tracker observation? (systolic for diastolic?)
                location = repository.getLocation(id, "Tracker");
                if (location.length()==0) {
                    System.out.println(id+" Obs");
                }
            }
            j++;
        }
    }

    public void TestObsNotFound(Repository repository) throws SQLException {
        List<Integer> ids = repository.getRows("filteredObservationsDelta");
        Integer j = 0; Integer id = 0;
        String result = "";
        while (ids.size() > j) {
            id = ids.get(j);

            result = repository.getObservationRS(id);
            System.out.println(result);

            if (result.length()==0) {
                System.out.println(id);
                Scanner scan = new Scanner(System.in);
                System.out.print("Press any key to continue . . . ");
                scan.nextLine();
            }

            j++;
        }
    }

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