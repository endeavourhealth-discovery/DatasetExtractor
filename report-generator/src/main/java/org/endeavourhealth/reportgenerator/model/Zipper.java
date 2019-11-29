package org.endeavourhealth.reportgenerator.model;

import lombok.Data;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Data
@Entity
public class Zipper extends AbstractEntity {

    private Boolean splitFiles = Boolean.TRUE;

    private String sourceDirectory;

    private String zipFilename;

    private Boolean switchedOn = Boolean.TRUE;

    private EncryptionMethod encryptionMethod = EncryptionMethod.AES;

    @Transient
    private String password;

    public boolean requiresPassword() {
        return password != null ? true : false;
    }

}
