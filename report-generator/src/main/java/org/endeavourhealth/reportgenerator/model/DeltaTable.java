package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;


@ToString
@Data
public class DeltaTable {

    @NotNull(message = "Delta Table name cannot be null")
    private String name;

    @NotNull(message = "Columns for delta cannot be null")
    private String columnsForDelta;
}
