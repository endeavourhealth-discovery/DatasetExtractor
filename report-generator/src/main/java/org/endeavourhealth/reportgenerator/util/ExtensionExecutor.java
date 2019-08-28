package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Extension;
import org.endeavourhealth.reportgenerator.model.ExtensionType;

@Slf4j
public class ExtensionExecutor {

    public void execute(Extension extension) {

        ExtensionType type = extension.getType();

        log.info("Executing extension {}", type.getDisplayName());

        log.info("with properties {}", extension.getProperties());

        switch( type ) {
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

