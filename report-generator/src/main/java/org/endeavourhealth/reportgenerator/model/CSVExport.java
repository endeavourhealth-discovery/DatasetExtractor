package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CSVExport {

    private List<Table> csvTablesToExport;
    private String outputDirectory;
    private Database database = Database.COMPASS;
    private Integer maxNumOfRowsInEachOutputFile = 0;  //0 is no limit
    private Boolean switchedOn = true;

}
