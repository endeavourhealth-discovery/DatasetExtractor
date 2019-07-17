package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SFTPUploader {


    public void upload(Report report, File file) throws Exception {

        ChannelSftp channel = getChannel(report);

        channel.put(file.getAbsolutePath(), "/ftp/" + getSFTPFileName());
    }

    private ChannelSftp getChannel(Report report) throws IOException, JSchException {
        JSch jSch = new JSch();

        jSch.addIdentity( report.getSftpPrivateKeyFile() );
//        jSch.setKnownHosts("/home/hal/known_hosts");

        Session session = jSch.getSession(report.getSftpUsername(), report.getSftpHostname(), report.getSftpPort());
// d2:dd:0f:44:d8:a2:85:a8:d1:6a:41:c9:55:91:38:72
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");

        channel.connect();

        return channel;
    }

    private String getSFTPFileName() {

        LocalDate localDate = LocalDate.now();

        String sftpFilename = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd") ) + ".csv";

        log.debug("SftpFilename : {}", sftpFilename);

        return sftpFilename;
    }
}

