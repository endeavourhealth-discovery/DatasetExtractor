package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
@ToString
public class Report {

    @NotNull
    @Length(min = 3, max = 100)
    private String name;

    //Validation
    private Boolean isValid;
    private Set<ConstraintViolation<Report>> constraintViolations;

    //Database
    @Valid
    private StoredProcedureExecutor storedProcedureExecutor;

    private Boolean active = true;
    private boolean success = false;

    //Extensions
    private List<@Valid Extension> extensions;

    //Schedule
    private Boolean isDaily = false;
    private String dayOfMonth;
    private String dayOfWeek;

    //CSV
    @Valid
    private CSVExport csvExport;

    //SFTP
    @Valid
    private SftpUpload sftpUpload;

    //FHIR
}
