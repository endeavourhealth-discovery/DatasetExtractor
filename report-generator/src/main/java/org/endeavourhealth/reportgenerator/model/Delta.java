package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ToString
@Slf4j
public class Delta {

    @NotEmpty(message = "Delta must have at least one table to export")
    private List<@Valid Table> tables;
}
