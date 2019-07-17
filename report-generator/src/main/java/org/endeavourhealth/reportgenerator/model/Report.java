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

    private Boolean requiresDeanonymising;
    private Boolean active = false;
    private boolean success;
    private Boolean isDaily = false;

    //CSV export
    private List<Table> csvTablesToExport;
    private String csvOutputDirectory;
    private String csvStagingDirectory;

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

