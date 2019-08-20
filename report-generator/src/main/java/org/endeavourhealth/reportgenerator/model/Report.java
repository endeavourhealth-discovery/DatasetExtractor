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
    private Boolean executeStoredProcedures = true;

    private Boolean requiresDeanonymising  = false;
    private Boolean active = false;
    private boolean success;

    //Schedule
    private Boolean isDaily = false;
    private String dayOfMonth;
    private String dayOfWeek;

    //CSV export
    private List<Table> csvTablesToExport;
    private String csvOutputDirectory;
    private String csvStagingDirectory;
    private Database csvExportDatabase = Database.COMPASS;
    private Integer maxNoOfRowsInEachFile = 0; //0 is no limit

    //SFTP upload
    private String sftpPrivateKeyFile;
    private String sftpHostname;
    private String sftpUsername;
    private Integer sftpPort;
    private String sftpFilename;
    private Boolean uploadSftp;

	public String getDatasetTableYesterday() {
		return datasetTable + "_yesterday";
	}

    public boolean runStoredProceduresInCompassDatabase() {
	    return storedProcedureDatabase.equals("compass") ? true : false;
    }

    public boolean exportCSVFromCompassDatabase() {
        return csvExportDatabase.equals("compass") ? true : false;
    }
}
