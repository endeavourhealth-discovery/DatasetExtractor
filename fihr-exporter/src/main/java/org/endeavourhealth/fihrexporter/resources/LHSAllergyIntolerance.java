package org.endeavourhealth.fihrexporter.resources;

import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;
import org.hl7.fhir.dstu3.model.*;

import org.endeavourhealth.fihrexporter.repository.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LHSAllergyIntolerance {

	private String getAllergyResource(Integer patientid, String clineffdate, String allergyname, String snomedcode, String PatientRef)
	{
		FhirContext ctx = FhirContext.forDstu3();

		AllergyIntolerance allergy = new AllergyIntolerance();

		allergy.getMeta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-AllergyIntolerance-1");
		allergy.setClinicalStatus(AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE);
		allergy.setVerificationStatus(AllergyIntolerance.AllergyIntoleranceVerificationStatus.CONFIRMED);

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			allergy.setAssertedDate(format.parse(clineffdate));
		} catch (Exception e) {
		}

		// manifestation or codeable concept?
		CodeableConcept code = new CodeableConcept();
		code.addCoding()
				.setCode(snomedcode)
				.setSystem("http://snomed.info/sct")
				.setDisplay(allergyname);

		allergy.setCode(code);

		allergy.setPatient(new Reference("Patient/" + PatientRef));

		String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(allergy);

		return encoded;
	}

	public String Run(Repository repository, String baseURL)  throws SQLException
	{
		String encoded = "";
		List<Integer> ids = repository.getRows("filteredallergies");

		String url = baseURL + "AllergyIntolerance";

		ResultSet rs;
		Integer id = 0; Integer j = 0;

		Integer nor = 0;
		String allergyname = ""; String snomedcode = ""; String clineffdate = "";
		String location=""; Integer typeid = 4;

		while (ids.size() > j) {

			id = ids.get(j);

			rs = repository.getAllergyIntoleranceRS(id);

			if (rs.next())
			{
				nor = rs.getInt("patient_id");
				clineffdate = rs.getString(3);
				allergyname = rs.getString(4);
				snomedcode = rs.getString(5);

				boolean prev = repository.PreviouslyPostedId(nor, "Patient");
				if (prev==false) {
					LHSPatient patient = new LHSPatient();
					patient.RunSinglePatient(repository, nor, baseURL);
				}

				location = repository.getLocation(nor, "Patient");
				if (location.length() == 0)
				{
					System.out.println("Unable to find patient " + nor);
					j++;
					continue;
				}

				encoded = getAllergyResource(nor,clineffdate,allergyname,snomedcode,location);

				LHShttpSend send = new LHShttpSend();
				Integer httpResponse = send.Post(repository,id, "", url, encoded, "AllergyIntolerance", nor, typeid);
			}

			j++;
		}
		return encoded;
	}
}