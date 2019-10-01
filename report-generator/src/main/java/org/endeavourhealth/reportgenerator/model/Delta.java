package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ToString
@Slf4j
@Entity
public class Delta extends AbstractEntity {

    @NotEmpty(message = "Delta must have at least one table to export")
    @OneToMany(cascade = CascadeType.ALL)
    private List<@Valid DeltaTable> tables;

    private Boolean switchedOn = true;
}
