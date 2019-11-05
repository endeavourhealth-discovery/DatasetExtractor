package org.endeavourhealth.reportgenerator.model;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class Zipper extends AbstractEntity {

    private Boolean splitFiles = Boolean.TRUE;

    private String sourceDirectory;

    private String zipFilename;
}
