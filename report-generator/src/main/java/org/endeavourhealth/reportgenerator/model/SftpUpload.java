package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
@AllArgsConstructor
public class SftpUpload {

    private String privateKeyFile;

    private String hostname;

    private String username;

    private Integer port;

    private String hostDirectory;

    private Boolean switchedOn = true;
}
