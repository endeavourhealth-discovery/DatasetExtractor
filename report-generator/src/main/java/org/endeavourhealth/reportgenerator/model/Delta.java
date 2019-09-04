package org.endeavourhealth.reportgenerator.model;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class Delta {

    @NotEmpty(message = "Delta must have at least one table to export")
    private List<@Valid Table> tables;
}
