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

        if(!extension.getSwitchedOn()) {
            log.info("Extension is switched off, nothing to do");
            return;
        }

        switch (type) {
            case DELTA:
                executeDelta(extension);
                break;
            case DEANONYMISE_WF:
                executeDeanonymiseWF(extension);
                break;
            case DEANONYMISE_INEL_IMMS:
                executeDeanonymiseINELImms(extension);
                break;
            case DEANONYMISE_ELGH:
                executeDeanonymiseELGH(extension);
                break;
            case DEANONYMISE_EYE:
                executeDeanonymiseEye(extension);
                break;
            case DEANONYMISE_FRAILTY:
                executeDeanonymiseFrailty(extension);
                break;
            case DEANONYMISE_WF_DIABETES:
                executeDeanonymiseWFDiabetes(extension);
                break;
            case DEANONYMISE_ELGH_PHASE_TWO:
                executeDeanonymiseELGHPhaseTwo(extension);
        }
    }

    //        - name: "BF_OUT_Royal_London"
    //        - name: "BF_OUT_Newham"
    //        - name: "BF_OUT_Whipps_Cross"

    private void executeDeanonymiseFrailty(Extension extension) {

        log.info("Running deanonymising of frailty, running...");

        repository.bootEntityManagerFactoryCore();

        deanonymise("BF_OUT_Royal_London");
        deanonymise("BF_OUT_Newham");
        deanonymise("BF_OUT_Whipps_Cross");

        log.info("...deanonymising all done");
    }

    private void deanonymise(String tableName) {

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIds(offset, tableName );

        while (pseudoIds.size() > 0) {

            repository.deanonymiseFrailty(pseudoIds, tableName);

            offset += 3000;

            pseudoIds = repository.getPseudoIds(offset, tableName );
        }
    }

    private void executeDeanonymiseEye(Extension extension) {

        log.info("Running deanonymising of Diabetes eye, running...");

        repository.bootEntityManagerFactoryCore();

        repository.bootEntityManagerFactoryTransform();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForEye(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseEYE(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForEye(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseELGH(Extension extension) {

        log.info("Running deanonymising of ELGH, running...");

        repository.bootEntityManagerFactoryCore();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForELGH(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseELGH(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForELGH(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseWF(Extension extension) {

        log.info("Report required deanonymising of Waltham Forest, running...");

        repository.bootEntityManagerFactoryCore();

        repository.bootEntityManagerFactoryTransform();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForWF(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseWF(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForWF(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseINELImms(Extension extension) {

        log.info("Report required deanonymising of INEL Imms, running...");

        repository.bootEntityManagerFactoryCore();

        repository.bootEntityManagerFactoryTransform();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForINELImms(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseINELImms(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForINELImms(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseWFDiabetes(Extension extension) {

        log.info("Report required deanonymising of Waltham Forest Diabetes, running...");

        repository.bootEntityManagerFactoryCore();

        repository.bootEntityManagerFactoryTransform();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForWFDiabetes(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseWFDiabetes(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForWFDiabetes(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDeanonymiseELGHPhaseTwo(Extension extension) {

        log.info("Report required deanonymising of East London Genes & Health Phase Two, running...");

        repository.bootEntityManagerFactoryCore();

        repository.bootEntityManagerFactoryTransform();

        Integer offset = 0;

        List<String> pseudoIds = repository.getPseudoIdsForELGHPhaseTwo(offset);

        while (pseudoIds.size() > 0) {

            repository.deanonymiseELGHPhaseTwo(pseudoIds);

            offset += 3000;

            pseudoIds = repository.getPseudoIdsForELGHPhaseTwo(offset);
        }

        log.info("...deanonymising all done");
    }

    private void executeDelta(Extension extension) {
    }
}
