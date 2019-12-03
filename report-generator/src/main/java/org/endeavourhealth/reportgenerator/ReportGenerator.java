package org.endeavourhealth.reportgenerator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectScheduleEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;
import org.endeavourhealth.csvexporter.CSVExporter;
import org.endeavourhealth.reportgenerator.beans.DSMConfiguration;
import org.endeavourhealth.reportgenerator.model.*;
import org.endeavourhealth.reportgenerator.repository.DSMRepository;
import org.endeavourhealth.reportgenerator.repository.JpaRepository;
import org.endeavourhealth.reportgenerator.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

@Slf4j
public class ReportGenerator implements AutoCloseable {

    private JpaRepository repository;

    private SFTPUploader sftpUploader;

    private final Properties properties;

    private final Scheduler scheduler;

    public ReportGenerator(Properties properties) {

        this.properties = properties;

        this.sftpUploader = new SFTPUploader();

        this.scheduler = new Scheduler();

        log.info("**** Booting org.endeavourhealth.reportgenerator.ReportGenerator, loading property file and db repository.....");

        log.info("**** ReportGenerator successfully booted!!");
    }

    public ReportGenerator(Properties properties, SFTPUploader sftpUploader) {
        this(properties);
        this.sftpUploader = sftpUploader;
    }

    public List<Report> generate(List<Report> reports) {

        for (Report report : reports) {

            if( !reportIsRunnable( report ) ) {
                continue;
            }

            try {
                executeReport(report);
            } catch (Exception e) {
                log.error("Report " + report + " has thrown exception", e);
                report.setErrorMessage( e.getMessage() );
            }

            report.setEndTime(LocalDateTime.now());
        }

        return reports;
    }

    private void populateFromDSM(Report report) throws Exception {
        String projectId = report.getDsmProjectId();

        DSMRepository dsmRepository = new DSMRepository();

        ProjectScheduleEntity scheduleEntity =  dsmRepository.getSchedule(projectId);

        List<OrganisationEntity> organisations = dsmRepository.getOrganisations(projectId);

        ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = dsmRepository.getExtractTechnicalDetailsEntity(projectId);

        DSMConfiguration dsmConfiguration = new DSMConfiguration();

        dsmConfiguration.setProjectScheduleEntity( scheduleEntity );
        dsmConfiguration.setOrganisations(organisations);
        dsmConfiguration.setExtractTechnicalDetailsEntity(extractTechnicalDetailsEntity);

        report.setDsmConfiguration( dsmConfiguration );
    }

    private boolean reportIsRunnable(Report report) {

        if (!report.getActive()) {
            log.warn("Report is inactive");
            return false;
        }
        if (!report.isValid()) {
            log.warn("Report is invalid {}", report.getErrors());
            return false;
        }
        if(!scheduler.isScheduled( report.getSchedule() )) {
            log.info("Report is not scheduled");
            return false;
        }

        return true;
    }


    private void executeReport(Report report) throws Exception {

        log.info("Generating report {}", report);

        // populateFromDSM( report );

        bootRepository(report);

        callStoredProcedures( report, true );

        executeExtensions( report );

        executeDeltas( report );

        callStoredProcedures( report, false );

        export( report );

        zipAndEncrypt( report );

        uploadToSftp( report );

        processAnalytics( report.getAnalytics() );

        //Not all reports have use of a database
        if(repository != null) repository.close();
    }



    private void processAnalytics(Analytics analytics) {

        if(analytics == null) {
            log.info("No analytics found, nothing to do here");
            return;
        }

        if(!analytics.getSwitchedOn()) {
            log.info("Analytics switched off, nothing to do");
        }

        repository.processAnalytics( analytics );
    }

    private void executeDeltas(Report report) {

        if(report.getDelta() == null) {
            log.info("No delta found, nothing to do here");
            return;
        }

        if( !report.getDelta().getSwitchedOn()) {
            log.info("Delta switched off, nothing to do here");
            return;
        }

        DeltaExecutor deltaExecutor = new DeltaExecutor( repository );

        deltaExecutor.execute( report.getDelta() );
    }

    private void executeExtensions(Report report) {

        if(report.getExtensions() == null || report.getExtensions().isEmpty()) {
            log.info("No extensions found, nothing to do here");
            return;
        }

        ExtensionExecutor extensionExecutor = new ExtensionExecutor( repository );

        for(Extension e : report.getExtensions()) {
            extensionExecutor.execute( e );
        }
    }

    private void bootRepository(Report report) throws SQLException {

        if(!report.requiresDatabase()) {
            log.info("Report doesn't required database, not booting repository");
            return;
        }

        this.repository = new JpaRepository(properties, report.getStoredProcedureExecutor().getDatabase());
    }

    private void uploadToSftp(Report report) throws Exception {

        SftpUpload sftpUpload = report.getSftpUpload();

        if(sftpUpload == null) {
            log.info("No configuration for sftp found, nothing to do here");
            return;
        }

        if (!sftpUpload.getSwitchedOn()) {
            log.info("SFTP switched off, nothing to do here");
            return;
        }

        File stagingDirectory = new File(properties.getProperty("csv.staging.directory"));

        sftpUploader.uploadDirectory(sftpUpload, stagingDirectory);
    }

    private void zipAndEncrypt(Report report) throws Exception {

        File stagingDirectory = new File(properties.getProperty("csv.staging.directory"));

        cleanDirectory(stagingDirectory);

        FileZipper fileZipper = new FileZipper(report, properties );

        fileZipper.zip();

        FileEncrypter fileEncrypter = new FileEncrypter();

        fileEncrypter.encryptDirectory( stagingDirectory );

    }

    private void export(Report report) throws Exception {

        Exporter exporter = new Exporter( report, properties );

        exporter.export();
    }



    private void callStoredProcedures(Report report, boolean isPre) {

        StoredProcedureExecutor storedProcedureExecutor = report.getStoredProcedureExecutor();

        if(storedProcedureExecutor == null) {
            log.info("Stored procedure execution is turned off");
            return;
        }

        if(!storedProcedureExecutor.getSwitchedOn()) {
            log.info("Stored procedure execution is turned off");
            return;
        }

        List<String> storedProcedures;

        if(isPre) {
          storedProcedures = storedProcedureExecutor.getPreStoredProcedures();
        } else {
          storedProcedures = storedProcedureExecutor.getPostStoredProcedures();
        }

        if (storedProcedures == null) {
            log.info("No stored procedures in report definition");
            return;
        }

        log.info("Cycling through stored procedures");

        for (String storedProcedure : storedProcedures) {
            repository.call(storedProcedure, storedProcedureExecutor);
        }

        log.info("Stored procedures all called");
    }

    @Override
    public void close() throws Exception {
        if(repository != null) repository.close();
    }
}
