package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.List;


@ToString
@Data
@Entity
@javax.persistence.Table(name = "db_table")
public class Table extends AbstractEntity{

    @NotNull(message = "CSV Table name cannot be null")
    private String name;

    @NotNull(message = "CSV Filename cannot be null")
    private String fileName;
}
