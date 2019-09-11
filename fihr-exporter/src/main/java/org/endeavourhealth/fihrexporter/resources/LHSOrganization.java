package org.endeavourhealth.fihrexporter.resources;

import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;
import org.hl7.fhir.dstu3.model.*;

import org.endeavourhealth.fihrexporter.repository.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LHSOrganization {

	private static String GetOrgResource(String odscode, String name, String postcode)
	{
		FhirContext ctx = FhirContext.forDstu3();

		Organization organization = null;
		organization = new Organization();

		organization.addIdentifier()
				.setSystem("https://fhir.nhs.uk/Id/ods-organization-code")
				.setValue(odscode);

		organization.setName(name);

		organization.addAddress()
				.setPostalCode(postcode);

		String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(organization);

		return encoded;
	}

	public String Run(Repository repository, Integer organization_id)  throws SQLException
	{
		ResultSet rs;
		rs = repository.getOrganizationRS(organization_id);

		String odscode = "";
		String name = "";
		String postcode = "";
		Integer id = 0;
		String encoded = "";

		String url = "http://apidemo.discoverydataservice.net:8080/fhir/STU3/Organization";

		if (rs.next()) {
			odscode = rs.getString("ods_code");
			name = rs.getString("name");
			postcode = rs.getString("postcode");
			id = rs.getInt("id");
			encoded = GetOrgResource(odscode, name, postcode);

			LHShttpSend send = new LHShttpSend();
			Integer httpResponse = send.Post(repository, id, "", url, encoded, "Organization", 0, 0);
		}
		return encoded;
	}
}