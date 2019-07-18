package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SFTPUploader {


    public void upload(Report report, File file) throws Exception {

        String removeFilename = getRemoteFilename( report );

        Session session = getSession( report );

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");

        sftpChannel.connect();

        sftpChannel.put(file.getAbsolutePath(),  removeFilename);

        sftpChannel.exit();

        session.disconnect();

        log.info("Successfully uploaded file {} to {}", removeFilename, report.getSftpHostname());
    }

    private Session getSession(Report report) throws JSchException {
        JSch jSch = new JSch();

        jSch.addIdentity( report.getSftpPrivateKeyFile() );

        log.debug("Opening sftp channel {} {}", report.getSftpHostname(), report.getSftpUsername());

//        jSch.setKnownHosts("/home/hal/known_hosts");

        Session session = jSch.getSession(report.getSftpUsername(), report.getSftpHostname(), report.getSftpPort());
// d2:dd:0f:44:d8:a2:85:a8:d1:6a:41:c9:55:91:38:72
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        return session;
    }

    private String getRemoteFilename(Report report) {

        String remoteFilename = report.getSftpFilename();

        if(remoteFilename.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

            remoteFilename = remoteFilename.replace("{today}", today);
        }

        remoteFilename = "/ftp/" + remoteFilename;

        log.info("Uploading file to sftp remote filename {}", remoteFilename);

        return remoteFilename;
    }
}

