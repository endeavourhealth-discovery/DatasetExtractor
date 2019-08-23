package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.util.List;


@Data
@ToString
@AllArgsConstructor
public class CSVExport {

    private List<Table> tables;

    private String outputDirectory;

    private Database database = Database.COMPASS;

    private Integer maxNumOfRowsInEachOutputFile = 0;  //0 is no limit

    private Boolean switchedOn = true;
}
