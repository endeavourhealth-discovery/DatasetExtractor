package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Report {
    private String name;

    //Database
    private List<String> preStoredProcedures;
    private List<String> postStoredProcedures;
    private Database storedProcedureDatabase = Database.COMPASS;
    private Boolean storedProceduresSwitchedOn = true;

    private Boolean requiresDeanonymising  = false;
    private Boolean active = true;
    private boolean success;

    //Schedule
    private Boolean isDaily = false;
    private String dayOfMonth;
    private String dayOfWeek;

    private CSVExport csvExport;

    //CSV export
    private List<Table> csvTablesToExport;
    private String csvOutputDirectory;
    private Database csvExportDatabase = Database.COMPASS;
    private Integer csvMaxNumOfRowsInEachOutputFile = 0; //0 is no limit
    private Boolean csvExportSwitchedOn = true;

    //SFTP upload
    private String sftpPrivateKeyFile;
    private String sftpHostname;
    private String sftpUsername;
    private Integer sftpPort;
    private String sftpHostDirectory;
    private Boolean sftpSwitchedOn = true;
}
