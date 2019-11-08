package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SftpUpload extends AbstractEntity {

    @NotNull
    private String privateKeyFile;

    private String hostfilename;

    @NotNull
    private String hostname;

    @NotNull
    private String username;

    private Integer port = 990;

    @NotNull
    private String hostDirectory;

    private Boolean switchedOn = true;
}
