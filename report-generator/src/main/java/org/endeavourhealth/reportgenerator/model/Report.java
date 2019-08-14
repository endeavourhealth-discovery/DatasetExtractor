package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Report {
    private String name;

    //Database
    private String datasetTable;
    private List<String> preStoredProcedures;
    private List<String> postStoredProcedures;
    private String storedProcedureDatabase = "compass";

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
    private String csvExportDatabase = "compass";

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
}

