package org.endeavourhealth.datasetextractor.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Report {
    private String name;
    private List<String> storedProcedures;
    private Boolean requiresDeanonymising;
    private String sftpAccount;
    private boolean success;
}
