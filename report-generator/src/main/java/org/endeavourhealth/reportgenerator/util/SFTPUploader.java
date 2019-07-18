package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SFTPUploader implements AutoCloseable {

    private Session session;

    private ChannelSftp channelSftp;


    public void upload(Report report, File file) throws Exception {

        String remoteFilename = getRemoteFilename( report );

        initSession( report );

        channelSftp.put(file.getAbsolutePath(),  remoteFilename);

        close();

        log.info("Successfully uploaded file {} to {} with user {}", remoteFilename, report.getSftpHostname(), report.getSftpUsername());
    }

    private void initSession(Report report) throws JSchException {
        JSch jSch = new JSch();

        jSch.addIdentity( report.getSftpPrivateKeyFile() );

        log.debug("Opening sftp channel {} {}", report.getSftpHostname(), report.getSftpUsername());

//        jSch.setKnownHosts("/home/hal/known_hosts");

        session = jSch.getSession(report.getSftpUsername(), report.getSftpHostname(), report.getSftpPort());

        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        channelSftp = (ChannelSftp) session.openChannel("sftp");

        channelSftp.connect();
    }

    private String getRemoteFilename(Report report) {

        String remoteFilename = report.getSftpFilename();

        if(remoteFilename.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

            remoteFilename = remoteFilename.replace("{today}", today);
        }

        remoteFilename = "/ftp/" + remoteFilename;

        return remoteFilename;
    }

    public void close() {
        channelSftp.exit();
        session.disconnect();
    }
}

