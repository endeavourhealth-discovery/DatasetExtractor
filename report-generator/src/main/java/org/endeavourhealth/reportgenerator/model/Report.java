package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import org.endeavourhealth.reportgenerator.beans.DSMConfiguration;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Report extends AbstractEntity {

    private LocalDateTime startTime = LocalDateTime.now();

    //Set to default in case report is not valid etc, so populate here, can be updated later
    private LocalDateTime endTime = LocalDateTime.now();

    @Transient
    private Analytics analytics;

    private String dsmProjectId;

    @Transient
    private DSMConfiguration dsmConfiguration;

    @NotNull
    @Length(min = 3, max = 100)
    private String name;

    @Transient
    private Set<ConstraintViolation<Report>> constraintViolations;

    @Valid
    @OneToOne(cascade = CascadeType.ALL)
    private StoredProcedureExecutor storedProcedureExecutor;

    private Boolean active = true;

    private String errorMessage;

    @Valid
    @OneToOne(cascade = CascadeType.ALL)
    private Schedule schedule;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "report_id")
    private List<@Valid Extension> extensions;

    @OneToOne(cascade = CascadeType.ALL)
    private Zipper zipper = new Zipper();

    //Delta
    @OneToOne(cascade = CascadeType.ALL)
    private Delta delta;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    //CSV
    @Valid
    @OneToOne(cascade = CascadeType.ALL)
    private CSVExport csvExport;

    //CSV
    @Valid
    @OneToOne(cascade = CascadeType.ALL)
    private ExcelExport excelExport;

    //SFTP
    @Valid
    @OneToOne(cascade = CascadeType.ALL)
    private SftpUpload sftpUpload;

    public String getOutputDirectory() {
      if(csvExport != null && csvExport.getOutputDirectory() != null) return csvExport.getOutputDirectory();
      if(excelExport != null && excelExport.getOutputDirectory() != null) return excelExport.getOutputDirectory();

      return null;
    }

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
        if(analytics != null && analytics.getSwitchedOn()) {
            return true;
        }

        //All extensions require database at present TODO check for switchedOn
        if(extensions != null && extensions.isEmpty() == false) return true;

        //Csv export uses own datasource, as was designed to be stand alone

        return false;
    }

    public boolean isValid() {
        return constraintViolations == null || constraintViolations.isEmpty() ? true : false;
    }

    public String getErrors() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ConstraintViolation<Report> constraintViolation : constraintViolations) {
            stringBuilder.append(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
        }

        return stringBuilder.toString();
    }

    public boolean isDeltaReport() {
        if(getDelta() != null && getDelta().getSwitchedOn()) return true;

        return false;
    }

    public boolean isStfpSwitchedOn() {
        if(getSftpUpload() != null && getSftpUpload().getSwitchedOn()) return true;
        return false;
    }
}
