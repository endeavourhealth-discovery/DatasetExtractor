package org.endeavourhealth.fihrexporter;

import org.endeavourhealth.fihrexporter.repository.Repository;
import org.endeavourhealth.fihrexporter.resources.LHSAllergyIntolerance;
import org.endeavourhealth.fihrexporter.resources.LHSMedicationStatement;
import org.endeavourhealth.fihrexporter.resources.LHSPatient;
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

        //do something
        System.out.println("test");

        //LHSPatient patient = new LHSPatient();
        //patient.Run(this.repository);

        //LHSMedicationStatement medicationStatement = new LHSMedicationStatement();
        //medicationStatement.Run(this.repository);

        LHSAllergyIntolerance allergyIntolerance = new LHSAllergyIntolerance();
        allergyIntolerance.Run(this.repository);

    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
