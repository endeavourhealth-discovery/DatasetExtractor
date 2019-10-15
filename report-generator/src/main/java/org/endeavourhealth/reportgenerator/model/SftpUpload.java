package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SftpUpload {

    @NotNull
    private String privateKeyFile;

    private String zipFilename;

    @NotNull
    private String hostname;

    @NotNull
    private String username;

    private Integer port = 990;

    @NotNull
    private String hostDirectory;

    private Boolean switchedOn = true;
}
