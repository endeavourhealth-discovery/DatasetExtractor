package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;


@ToString
@Data
@Entity
public class DeltaTable extends AbstractEntity {

    @NotNull(message = "Delta Table name cannot be null")
    private String name;

    @NotNull(message = "Columns to hash for delta cannot be null")
    private String columnsToHash;

    private String uniqueIdentifier = "id";

    private Boolean deleteUniqueIdentifier = Boolean.TRUE;


}
