package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;


@ToString
@Data
public class Table {

    @NotNull(message = "CSV Table name cannot be null")
    private String name;

    @NotNull(message = "CSV Filename cannot be null")
    private String fileName;
}
