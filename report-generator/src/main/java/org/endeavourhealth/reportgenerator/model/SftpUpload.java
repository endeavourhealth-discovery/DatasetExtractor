package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SftpUpload {

    private String privateKeyFile;

    private String hostname;

    private String username;

    private Integer port;

    private String hostDirectory;

    private Boolean switchedOn = true;
}
