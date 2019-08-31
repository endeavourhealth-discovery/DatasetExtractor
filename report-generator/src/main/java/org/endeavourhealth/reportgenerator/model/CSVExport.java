package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CSVExport {

    @NotEmpty
    private List<Table> tables;

    @NotNull
    private String outputDirectory;

    private Database database = Database.COMPASS;

    private Integer maxNumOfRowsInEachOutputFile = 0;  //0 is no limit

    private Boolean switchedOn = true;
}
