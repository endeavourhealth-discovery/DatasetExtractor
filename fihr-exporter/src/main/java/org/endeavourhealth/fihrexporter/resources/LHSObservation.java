package org.endeavourhealth.fihrexporter.resources;

import ca.uhn.fhir.context.FhirContext;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;
import org.hl7.fhir.dstu3.model.*;

import org.endeavourhealth.fihrexporter.repository.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LHSObservation {

	private CodeableConcept addCodeableConcept(String snomed, String term)
	{
		CodeableConcept code = new CodeableConcept();
		code.addCoding()
				.setCode(snomed)
				.setDisplay(term)
				.setSystem("http://snomed.info/sct");

		return code;
	}

	private Observation.ObservationComponentComponent ObsCompComp(String coreconceptid, String term, String resultvalue, String resultvalueunits)
	{
		Observation.ObservationComponentComponent occ= new Observation.ObservationComponentComponent();
		CodeableConcept codecc = new CodeableConcept();
		codecc.addCoding()
				.setCode(coreconceptid)
				.setSystem("http://snomed.info/sct")
				.setDisplay(term);
		occ.setCode(codecc);

		Quantity q = new Quantity();
		q.setValue(Double.parseDouble(resultvalue));
		q.setSystem("http://unitsofmeasure.org");
		if (resultvalueunits !=null) {q.setCode(resultvalueunits);}
		occ.setValue(q);

		return occ;
	}

	private String getObervationResource(Repository repository, Integer patientid, String snomedcode, String orginalterm, String resultvalue, String clineffdate, String resultvalunits, String PatientRef, String ids, Integer parent)
	{
		String id = "";

		//Observation observation = null;

		FhirContext ctx = FhirContext.forDstu3();

		Observation observation = new Observation();

		observation.setStatus(Observation.ObservationStatus.FINAL);

		String ObsRec = ""; String noncoreconceptid = "";

		// use parent code if necessary
		if (parent !=0) {
			try {

				ObsRec= repository.getObservationRecord(Integer.toString(parent));

				String[] ss = ObsRec.split("\\~");

				noncoreconceptid = ss[0]; orginalterm = ss[1];
				if (noncoreconceptid.length()==0) noncoreconceptid = ss[5];

				CodeableConcept code = addCodeableConcept(noncoreconceptid, orginalterm);
				observation.setCode(code);

				//System.out.println(ObsRec);
			} catch (Exception e) {
			}
		}

		if (parent == 0) {
			CodeableConcept code = addCodeableConcept(snomedcode, orginalterm);
			observation.setCode(code);
		}

		// http://hl7.org/fhir/stu3/valueset-observation-category.html
        // social-history, vital-signs, imaging, laboratory, procedure, survey, exam, therapy

        /*
		CodeableConcept vital = new CodeableConcept();
		vital.addCoding()
				.setCode("vital-signs");

		// might be a lab result, or something else?
		observation.addCategory(vital);
        */

		observation.setSubject(new Reference("/Patient/" + PatientRef));

		Period period = new Period();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			period.setStart(format.parse(clineffdate));
		} catch (Exception e) {
		}
		observation.setEffective(period);

		// nests codeable concepts
		String encoded = "";
		ArrayList occs=new ArrayList();

		if (ids.length() > 0) {
			String[] ss = ids.split("\\~");
			for (int i = 0; i < ss.length; i++) {
				id = ss[i];
				try {

					ObsRec = repository.getObservationRecord(id);

					if (ObsRec.length() == 0) {continue;}
					String obs[] = ObsRec.split("\\~");
					snomedcode = obs[0]; orginalterm = obs[1]; resultvalue = obs[2]; clineffdate = obs[3]; resultvalunits = obs[4];
					if (snomedcode.length() == 0) snomedcode = obs[5];
					if (resultvalue.length() > 0 || resultvalunits.length() > 0) {
                        Observation.ObservationComponentComponent ocs = ObsCompComp(snomedcode, orginalterm, resultvalue, resultvalunits);
                        occs.add(ocs);
                        observation.setComponent(occs);
                    }
				} catch (Exception e) {
				}
			}

			encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation);
			return encoded;
		}

        System.out.println(resultvalue.length());

		if (resultvalue.length()>0) {
            Observation.ObservationComponentComponent ocs = ObsCompComp(snomedcode, orginalterm, resultvalue, resultvalunits);
            occs.add(ocs);
            observation.setComponent(occs);
        }

		encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation);

		return encoded;
	}

	private void ObsAudit(Repository repository, String ids, Integer patientid) throws SQLException
	{
		String[] ss = ids.split("\\~");
		String id = "";
		for (int i = 0; i < ss.length; i++) {
			id = ss[i];
			repository.Audit(Integer.parseInt(id), "", "Tracker", 0, "dum", "", patientid, 0);
		}
	}

	public void DT(String prefix) {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
        String str = ts.toString();
        System.out.println(prefix+" "+str);
	}

	public String Run(Repository repository, String baseURL) throws SQLException
	{
		String encoded = ""; Integer j = 0; Integer id = 0;
		List<Integer> ids = repository.getRows("filteredobservations");

		Integer nor =0; // patientid
		String snomedcode =""; String orginalterm=""; String result_value="";
		String clineffdate = ""; String resultvalunits = ""; String location="";
		Integer typeid = 11; String t = ""; Integer parent =0; String parentids = "";

        String url = baseURL + "Observation";

		ResultSet rs; String result = "";

        Runtime gfg = Runtime.getRuntime();
        long memory1, memory2;
        Integer integer[] = new Integer[1000];

        while (ids.size() > j) {
			id = ids.get(j);

            System.out.println(id);

			if (id == 114870) {
				System.out.println("test");
			}

            result = repository.getObservationRS(id);

            if (result.length()>0) {

                String[] ss = result.split("\\~");
                nor = Integer.parseInt(ss[0]); snomedcode=ss[1]; orginalterm=ss[2]; result_value=ss[3]; clineffdate=ss[4]; resultvalunits=ss[5];

				// obs id sent in this run?  might have already been sent in a bp?
				t = repository.getLocation(id,"Tracker");
				if (t.length() > 0) {
					System.out.println("Obs" + id + " has been processed");
					j++;
					continue;
				}

				// parent = rs.getInt("parent_observation_id");
                parent = Integer.parseInt(ss[6]); parentids = "";
				if (parent != 0) {
					// find the other event with the same parent id
					parentids = repository.getIdsFromParent(parent);
					//System.out.println(ids);
				}

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

				encoded = getObervationResource(repository, nor, snomedcode, orginalterm, result_value, clineffdate, resultvalunits, location, parentids, parent);

				// post
				Integer httpResponse;
				LHShttpSend send = new LHShttpSend();

				httpResponse = send.Post(repository, id, "", url, encoded, "Observation", nor, typeid);

				if (parentids.length() > 0) {ObsAudit(repository, parentids, nor);}

				System.out.println(httpResponse.toString());

				j++;
			}
		}
		return encoded;
	}
}