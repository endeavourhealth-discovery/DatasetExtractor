package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CSVExport extends AbstractEntity {

    @NotEmpty(message = "CSV export must have at least one table to export")
    @OneToMany(cascade = CascadeType.ALL)
    private List<@Valid Table> tables;

    @NotNull
    private String outputDirectory;

    @Enumerated(EnumType.STRING)
    @Column(name = "db")
    private Database database = Database.COMPASS;

    private Integer maxNumOfRowsInEachOutputFile = 0;  //0 is no limit

    private Boolean switchedOn = true;
}
