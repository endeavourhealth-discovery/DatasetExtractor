package org.endeavourhealth.fihrexporter.resources;

import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;
import org.hl7.fhir.dstu3.model.*;

import org.endeavourhealth.fihrexporter.repository.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LHSMedicationStatement {
	private Dosage addDosage(String dosagetext, String qtyvalue, String qtyunit)
	{
		Dosage dose = null;

		dose = new Dosage();
		dose.setText(dosagetext);


		if ( (qtyvalue != null) & (qtyunit != null) ) {
			dose.setDose(new SimpleQuantity()
					//.setValue(Integer.parseInt(qtyvalue))
					.setValue(Double.parseDouble(qtyvalue))
					.setUnit(qtyunit)
			);
		}

		return dose;
	}

	private String GetMedicationStatementResource(Integer patientid, String dose, String quantityvalue, String quantityunit, String clinicaleffdate, String medicationname, String snomedcode, String PatientRef, String rxref)
	{
		FhirContext ctx = FhirContext.forDstu3();

		// MedicationStatement rxstatement = null;

		MedicationStatement rxstatement = new MedicationStatement();

		rxstatement.getMeta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-MedicationStatement-1");

		// this needs to be a switch statement using ?
		rxstatement.setStatus(MedicationStatement.MedicationStatementStatus.ACTIVE);

		rxstatement.setSubject(new Reference("/api/Patient/33"));

		rxstatement.setTaken(MedicationStatement.MedicationStatementTaken.UNK);

		rxstatement.setSubject(new Reference("Patient/" + PatientRef));

		rxstatement.setMedication(new Reference("Medication/" + rxref)
				.setDisplay(medicationname));

		ArrayList dosages=new ArrayList();

		Dosage doseage = addDosage(dose, quantityvalue, quantityunit);
		dosages.add(doseage);
		rxstatement.setDosage(dosages);

		String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(rxstatement);

		return encoded;
	}

	public String Run(Repository repository, String baseURL)  throws SQLException
	{
		String encoded = "";
		Integer id = 0; Integer j = 0;

		List<Integer> ids = repository.getRows("filteredmedications");

		ResultSet rs; String result;

		Integer nor = 0;
		String snomedcode = ""; String drugname = "";

		String dose = ""; String quantityvalue; String quantityunit = "";
		String clinicaleffdate = ""; String location = ""; Integer typeid = 10;

		String url = baseURL + "MedicationStatement";

		while (ids.size() > j) {

			id = ids.get(j);

			result = repository.getMedicationStatementRS(id);

			if (result.length()>0) {

				String[] ss = result.split("\\~");
				nor = Integer.parseInt(ss[0]);
				snomedcode = ss[1];
				drugname = ss[2];

				boolean prev = repository.PreviouslyPostedId(nor, "Patient");
				if (prev==false) {
					LHSPatient patient = new LHSPatient();
					patient.RunSinglePatient(repository, nor, baseURL);
				}

				prev = repository.PreviouslyPostedCode(snomedcode,"Medication");
				if (prev == false) {
					LHSMedication medication = new LHSMedication();
					medication.Run(repository, snomedcode, drugname);
				}

				location = repository.getLocation(nor, "Patient");
				if (location.length() == 0)
				{
					System.out.println("Unable to find patient " + nor);
					j++;
					continue;
				}

				String rxref = repository.GetMedicationReference(snomedcode);
				if (rxref.length() == 0)
				{
					System.out.println("Unable to find medication reference" + snomedcode);
					j++;
					continue;
				}

				dose=ss[3]; quantityvalue=ss[4]; quantityunit=ss[5]; clinicaleffdate=ss[6]; id= Integer.parseInt(ss[7]);

				encoded = GetMedicationStatementResource(nor, dose, quantityvalue, quantityunit, clinicaleffdate, drugname, snomedcode, location, rxref);
				System.out.println(encoded);

				LHShttpSend send = new LHShttpSend();
				Integer httpResponse = send.Post(repository, id, "", url, encoded, "MedicationStatement", nor, typeid);
			}

			j++;

			System.out.println(id);
		}

		return encoded;
	}
}