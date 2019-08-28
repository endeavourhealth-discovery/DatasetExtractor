package org.endeavourhealth.reportgenerator.util;

import org.endeavourhealth.reportgenerator.model.Extension;

public class ExtensionExecutor {

    public void execute(Extension extension) {

        switch(extension.getType()) {
            case DELTA:
                executeDelta( extension );
                break;
            case DEANONYMISE:
                executeDeanonymise( extension );
            }
        }

    private void executeDeanonymise(Extension extension) {
    }

    private void executeDelta(Extension extension) {
    }
}

