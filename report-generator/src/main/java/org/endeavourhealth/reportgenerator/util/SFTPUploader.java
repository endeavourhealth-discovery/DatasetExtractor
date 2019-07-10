package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Properties;

@Slf4j
public class SFTPUploader {

    private final String outputDirectory;

    public SFTPUploader(Properties properties) {
        outputDirectory = properties.getProperty("output.directory");
    }

    public void upload(Report report) throws Exception {

        String path = outputDirectory + File.separator + report.getName() + ".enc";

        File file = new File( path );

        sftp(report, file);
    }

    private void sftp(Report report, File file) throws JSchException, IOException, SftpException {
        JSch jSch = new JSch();

        File prvKeyFile = new File(report.getSftpPrivateKeyFile());

        String prvKey = FileUtils.readFileToString( prvKeyFile, (String) null);

        String pw = "";

        jSch.addIdentity( report.getSftpPrivateKeyFile() );

        Session session = jSch.getSession(report.getSftpUsername(), report.getSftpHostname(), report.getSftpPort());
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        String sftpFilename = getSFTPFileName();

        channel.put(file.getAbsolutePath(), "/ftp/" + sftpFilename);
    }

    private String getSFTPFileName() {

        LocalDate localDate = LocalDate.now();

        String sftpFilename = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd") ) + ".csv";

        log.debug("SftpFilename : {}", sftpFilename);

        return sftpFilename;
    }
}

