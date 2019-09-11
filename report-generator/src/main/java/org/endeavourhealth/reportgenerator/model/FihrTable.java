package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ToString
@Data
public class FihrTable {

    @NotNull(message = "Fihr table name cannot be null")
    private String name;

}
