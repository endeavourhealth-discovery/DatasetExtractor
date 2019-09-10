package org.endeavourhealth.fihrexporter;

import org.endeavourhealth.fihrexporter.repository.Repository;

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

    }

    @Override
    public void close() throws Exception {
        repository.close();
    }
}
