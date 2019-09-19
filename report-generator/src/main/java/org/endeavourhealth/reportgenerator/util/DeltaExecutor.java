package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.*;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;

import java.io.File;
import java.sql.SQLException;
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

      log.info("Executing delta {}", delta);

      for(DeltaTable dt : delta.getTables()) {
        repository.call( dt );
      }
    }

    public void close() throws SQLException {
        repository.close();
    }
}
