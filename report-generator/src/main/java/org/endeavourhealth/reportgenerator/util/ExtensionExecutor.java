package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Extension;
import org.endeavourhealth.reportgenerator.model.ExtensionType;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;

import java.util.List;

@Slf4j
public class ExtensionExecutor {

    private final JpaRepository repository;

    public ExtensionExecutor(JpaRepository repository) {
        this.repository = repository;
    }

    public void execute(Extension extension) {

        ExtensionType type = extension.getType();

        log.info("Executing extension {}", extension);

        switch (type) {
            case DELTA:
                executeDelta(extension);
                break;
            case DEANONYMISE_WF:
                executeDeanonymiseWF(extension);
                break;
            case DEANONYMISE_ELGH:
                executeDeanonymiseELGH(extension);
                break;
        }
    }

    private void executeDeanonymiseELGH(Extension extension) {
        log.info("Running deanonymising of ELGH, running...");

        repository.bootEntityManagerFactoryCore();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymise(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIds(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseWF(Extension extension) {

        log.info("Report required deanonymising of Waltham Forest, running...");

        repository.bootEntityManagerFactoryCore();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymise(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIds(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDelta(Extension extension) {
    }
}

