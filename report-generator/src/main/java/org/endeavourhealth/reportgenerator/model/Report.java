package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Report {
    private String name;

    private String datasetTable;

    private List<String> storedProcedures;

    private Boolean requiresDeanonymising;

    private Boolean active;

    private String sftpPrivateKeyFile;
    private String sftpHostname;
    private String sftpUsername;
    private Integer sftpPort;

    private boolean success;

	public String getDatasetTableYesterday() {
		return datasetTable + "_yesterday";
	}
}
