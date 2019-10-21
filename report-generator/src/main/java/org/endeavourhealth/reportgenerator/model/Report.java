package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class Report {


    private LocalDateTime startTime = LocalDateTime.now();

    private LocalDateTime endTime;

    @NotNull
    @Length(min = 3, max = 100)
    private String name;

    //Validation
    private Set<ConstraintViolation<Report>> constraintViolations;

    //Database
    @Valid
    private StoredProcedureExecutor storedProcedureExecutor;

    private Boolean active = true;
    private String errorMessage;

    private Schedule schedule;

    //Extensions
    private List<@Valid Extension> extensions;

    //Delta
    private Delta delta;

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

    public boolean isValid() {
        return constraintViolations.isEmpty() ? true : false;
    }

    public String getStatus() {
        if(!active) {
            return "Inactive";
        }

        if(errorMessage != null){
            return "Failure";
        }

        if(!isValid()) {
            return "Invalid Configuration";
        }

        return "Success";
    }

    public String getErrors() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ConstraintViolation<Report> constraintViolation : constraintViolations) {
            stringBuilder.append(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
        }

        return stringBuilder.toString();
    }

    public boolean isDelta() {

        if(getDelta() != null && getDelta().getSwitchedOn()) return true;

        return false;
    }

    //FHIR
}
