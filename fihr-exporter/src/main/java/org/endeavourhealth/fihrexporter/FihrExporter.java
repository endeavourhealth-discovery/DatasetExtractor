package org.endeavourhealth.fihrexporter;

import org.endeavourhealth.fihrexporter.repository.Repository;
import org.endeavourhealth.fihrexporter.resources.*;
import org.endeavourhealth.fihrexporter.resources.LHSMedicationStatement;
import org.endeavourhealth.fihrexporter.send.LHShttpSend;

import java.util.Properties;

public class FihrExporter implements AutoCloseable {

    private final Repository repository;

    public FihrExporter(final Properties properties) throws Exception {
        this(properties, new Repository(properties));
    }

    public FihrExporter(final Properties properties, final Repository repository) {
        this.repository = repository;
    }


    public void export() throws Exception {

        //Runtime gfg = Runtime.getRuntime();
        //long memory1, memory2;
        //Integer integer[] = new Integer[1000];

        //String baseURL = "http://apidemo.discoverydataservice.net:8080/fhir/STU3/";

        //System.out.println(">>>> " + this.repository.getBaseURL());
        String baseURL = this.repository.getBaseURL();

        //Integer httpResponse;

        if (baseURL.contains("https:")) {
            LHShttpSend send = new LHShttpSend();
            repository.token = send.GetToken(this.repository);
            LHSTest test = new LHSTest();
            String response = test.TestCert(repository.token, baseURL + "Patient/");
            if (response == "invalid-cert") {
                return;
            }
        }

        //repository.token="expired";

        // ** TO DO put this back in
        //this.repository.DeleteTracker();

        // ** TO DO put this back in
        //this.repository.DeleteFileReferences();

        //LHSPatient patient = new LHSPatient();
        //patient.Run(this.repository, baseURL);

        //LHSMedicationStatement medicationStatement = new LHSMedicationStatement();
        //medicationStatement.Run(this.repository, baseURL);

        //LHSAllergyIntolerance allergyIntolerance = new LHSAllergyIntolerance();
        //allergyIntolerance.Run(this.repository, baseURL);

        //gfg.gc();

        //LHSObservation observation = new LHSObservation();
        //observation.Run(this.repository, baseURL);

        //LHSTest test = new LHSTest();
        //test.Run(this.repository);
        //test.TestObsNotFound(this.repository);
        //test.ReconcileObservations(this.repository);
        //test.ReconcileOtherTables(this.repository);
        //test.GetPatients(this.repository);
        //String token = test.GetToken(this.repository);
        //String response = test.TestCert(this.repository.token, "https://dhs-fhir-test.azurehealthcareapis.com/Patient/");
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
