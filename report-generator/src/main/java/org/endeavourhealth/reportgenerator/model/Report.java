package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Report {
    private String name;

    //Database
    private StoredProcedureExecutor storedProcedureExecutor;

    private Boolean requiresDeanonymising  = false;
    private Boolean active = true;
    private boolean success;

    //Extensions
    List<Extension> extensions;

    //Schedule
    private Boolean isDaily = false;
    private String dayOfMonth;
    private String dayOfWeek;

    //CSV
    private CSVExport csvExport;

    //SFTP
    private SftpUpload sftpUpload;

    //FHIR
}
