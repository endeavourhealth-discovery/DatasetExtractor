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

    //Delta
    private Delta delta;

    //Schedule
    private Boolean isDaily = false;
    private String dayOfMonth;
    private String dayOfWeek;

    //CSV
    @Valid
    private CSVExport csvExport;

    //CSV
    @Valid
    private FihrExport fihrExport;

    //SFTP
    @Valid
    private SftpUpload sftpUpload;

    public boolean requiresDatabase() {
        //Filter not needed, but more explicit if declared here
        if(extensions != null && extensions.stream().filter( e -> e.getSwitchedOn() ).anyMatch( e -> e.requiresDatabase()) ) {
            return true;
        }
        if(storedProcedureExecutor != null && storedProcedureExecutor.requiresDatabase()) {
            return true;
        }
        if(delta != null && delta.getSwitchedOn()) {
            return true;
        }

        //All extensions require database at present TODO check for switchedOn
        if(extensions != null && extensions.isEmpty() == false) return true;

        //Csv export uses own datasource, as was designed to be stand alone

        return false;
    }

    //FHIR
}
