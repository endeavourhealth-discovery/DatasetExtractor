package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

@Slf4j
public class SFTPUploader implements AutoCloseable {

    private Session session;

    private ChannelSftp channelSftp;


    public void upload(Report report, File file) throws Exception {

        String remoteFilename = getRemoteFilename( report );

        logger.info("SFTP upload started to directory {} to {} with user {}", remoteFilename, report.getSftpHostname(), report.getSftpUsername());

        initSession( report );

        for (File f : file.listFiles()) {
            logger.info("Uploading file {}", f.getName());
            channelSftp.put(file.getAbsolutePath(), remoteFilename);
        }

        close();

        log.info("SFTP upload successful!");
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

    public void deleteFiles(String directory) throws SftpException {

        channelSftp.cd(directory);

        Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(".");

        for (ChannelSftp.LsEntry t : fileList) {

            log.debug("Deleting file {}/{}", directory, t.getFilename());

            channelSftp.rm(t.getFilename());
        }
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

