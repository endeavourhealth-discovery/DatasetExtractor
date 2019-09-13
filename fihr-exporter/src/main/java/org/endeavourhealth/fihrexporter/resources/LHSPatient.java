package org.endeavourhealth.fihrexporter.resources;

import com.mysql.cj.protocol.Resultset;
import org.endeavourhealth.fihrexporter.repository.Repository;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;
import org.hl7.fhir.dstu3.model.*;

import org.endeavourhealth.fihrexporter.resources.LHSOrganization;

public class LHSPatient {

	private static String getPatientResource(Integer PatId, String nhsNumber, String dob, String dod, String add1, String add2, String add3, String add4, String city, String startdate, String gender, String title, String firstname, String lastname, String telecom, String orglocation)
	{
		FhirContext ctx = FhirContext.forDstu3();

		Patient patient = new Patient();

		patient.getMeta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1");

		// nhsNumber
		Identifier nhs = patient.addIdentifier()
				.setSystem("https://fhir.nhs.uk/Id/nhs-number")
				.setValue(nhsNumber);
		CodeableConcept code = new CodeableConcept();
		code.addCoding()
				.setCode("01")
				.setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1");

		nhs.addExtension()
				.setUrl("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1")
				.setValue(code);

		if (gender.equals("Other")) {
			patient.setGender(Enumerations.AdministrativeGender.OTHER);
		}
		if (gender.equals("Male")) {
			patient.setGender(Enumerations.AdministrativeGender.MALE);
		}
		if (gender.equals("Female")) {
			patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		}
		if (gender.equals("Unknown")) {
			patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
		}

		patient.addName()
				.setFamily(lastname)
				.addPrefix(title)
				.addGiven(firstname)
				.setUse(HumanName.NameUse.OFFICIAL);

		// contact_type~contact_use~contact_value|
		if (telecom !="") {
			String[] ss = telecom.split("\\|");
			String z = "";
			for (int i = 0; i < ss.length; i++) {
				z = ss[i];
				String[] contact = z.split("\\~");
				ContactPoint t = new ContactPoint();

				t.setValue(contact[0]);

				if (contact[2].equals("Mobile")) t.setUse(ContactPoint.ContactPointUse.MOBILE);
				if (contact[2].equals("Home")) t.setUse(ContactPoint.ContactPointUse.HOME);

				if (contact[1].equals("Email")) t.setSystem(ContactPoint.ContactPointSystem.EMAIL);
				if (contact[1].equals("Phone")) t.setSystem(ContactPoint.ContactPointSystem.PHONE);

				patient.addTelecom(t);
			}
		}

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			patient.setBirthDate(format.parse(dob));
		} catch (Exception e) {
		}

		try {
			patient.setDeceased(new DateTimeType(dod));
		} catch (Exception e) {
		}

		patient.addAddress()
				.addLine(add1)
				.addLine(add2)
				.addLine(add3)
				.addLine(add4)
				.addLine(city);

		Extension registration = patient.addExtension();
		registration.setUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-RegistrationDetails-1");

		Period period = new Period();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			period.setStart(format.parse(startdate));
		} catch (Exception e) {
		}

		patient.setManagingOrganization(new Reference("Organization/" + orglocation));

		String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);

		return encoded;
	}

	public void RunSinglePatient(Repository repository, Integer nor, String baseURL)  throws SQLException {
		ResultSet rs;

		rs = repository.getPatientRS(nor);

		String url = baseURL + "Patient";

		int j = 0;
		List row;

		String nhsno = ""; String title  = ""; String dob = ""; String dod = ""; String add1 = ""; String add2 = "";
		String add3 = ""; String add4 = ""; String city = ""; String startdate = ""; String gender = "";
		String contacttype = ""; String contactuse = ""; String contactvalue = "";
		String firstname =""; String lastname = ""; String telecom = ""; String query = "";
		String odscode = ""; String orgname = ""; String orgpostcode = ""; Integer orgid = 0;
		Integer typeid = 2; String encoded = "";

		boolean prev; String orglocation;

		if (rs.next()) {
			orgid = rs.getInt("organization_id");

			// has the organization been sent for this patient?
			prev = repository.PreviouslyPostedId(orgid, "Organization");

			if (prev == false) {
				LHSOrganization organization = new LHSOrganization();
				organization.Run(repository, orgid);
			}

			// get the http location of the organization_id
			orglocation = repository.getLocation(orgid, "Organization");
			if (orglocation.length() == 0) {
				System.out.println("Cannot find patients " + nor + " organization?");
				return;
			}

			nhsno = rs.getString(2);
			dob = rs.getString("date_of_birth");
			odscode = rs.getString("ods_code");
			orgname = rs.getString("org_name");
			orgpostcode = rs.getString("org_postcode");

			telecom = repository.GetTelecom(nor);

			dod = rs.getString("date_of_death");
			add1 = rs.getString(9);
			add2 = rs.getString(10);
			add3 = rs.getString(11);
			add4 = rs.getString(12);
			city = rs.getString(13);
			gender = rs.getString("gender");
			contacttype = rs.getString("contact_type");
			contactuse = rs.getString("contact_use");
			contactvalue = rs.getString("contact_value");
			title = rs.getString("title");
			firstname = rs.getString("first_names");
			lastname = rs.getString("last_name");

			startdate = rs.getString(11); // date added to the cohort?

			encoded = getPatientResource(nor, nhsno, dob, dod, add1, add2, add3, add4, city, startdate, gender, title, firstname, lastname, telecom, orglocation);

			LHShttpSend send = new LHShttpSend();
			Integer httpResponse = send.Post(repository, nor, "", url, encoded, "Patient", nor, typeid);
		}
	}

	public void Run(Repository repository, String baseURL) {
		try {
			// List<List<String>> patient = repository.getPatientRows();

			List<Integer> patient = repository.getPatientRows();

			int j = 0;
			List row;

			// String nor;
			Integer nor;

			String url = baseURL + "Patient";

			while (patient.size() > j) {
				// nor = patient.get(j).get(1);
				nor = patient.get(j);
				System.out.println(nor);

				RunSinglePatient(repository, nor, baseURL);

				j++;
			}
		}catch(Exception e){ System.out.println(e);}
	}
}