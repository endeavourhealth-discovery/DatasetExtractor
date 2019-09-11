package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FihrExport {

    @NotEmpty(message = "Fihr export must have at least one table to export")
    private List<@Valid FihrTable> tables;

    private Boolean switchedOn = true;
}
