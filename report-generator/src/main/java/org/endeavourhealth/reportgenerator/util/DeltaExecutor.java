package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Delta;
import org.endeavourhealth.reportgenerator.model.Extension;
import org.endeavourhealth.reportgenerator.model.ExtensionType;
import org.endeavourhealth.reportgenerator.model.SftpUpload;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

@Slf4j
public class DeltaExecutor implements AutoCloseable {

    private final JpaRepository repository;

    public DeltaExecutor(JpaRepository repository) {
        this.repository = repository;
    }

    public void execute(Delta delta) {

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
            case DEANONYMISE_ELGH:
                executeDeanonymiseELGH(extension);
                break;
        }
    }
    public void close() {
        channelSftp.exit();
        session.disconnect();
    }
}
