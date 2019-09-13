package org.endeavourhealth.fihrexporter;

import org.endeavourhealth.fihrexporter.repository.Repository;
import org.endeavourhealth.fihrexporter.resources.*;
import org.endeavourhealth.fihrexporter.resources.LHSMedicationStatement;

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

        // ** TO DO needs to be got from config (indexed by organization id)
        String baseURL = "http://apidemo.discoverydataservice.net:8080/fhir/STU3/";

        this.repository.DeleteTracker();

        LHSPatient patient = new LHSPatient();
        patient.Run(this.repository, baseURL);

        LHSMedicationStatement medicationStatement = new LHSMedicationStatement();
        medicationStatement.Run(this.repository, baseURL);

        LHSAllergyIntolerance allergyIntolerance = new LHSAllergyIntolerance();
        allergyIntolerance.Run(this.repository, baseURL);

        LHSObservation observation = new LHSObservation();
        observation.Run(this.repository, baseURL);

        //LHSTest test = new LHSTest();
        //test.Run(this.repository);

    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
