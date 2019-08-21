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

    public void uploadDirectory(Report report, File directory) throws Exception {

        String remoteDirectory = report.getSftpHostDirectory();

        log.info("SFTP upload started to directory {} on host {} with user {}", remoteDirectory, report.getSftpHostname(), report.getSftpUsername());

        initSession( report );

        for (File file : directory.listFiles()) {
            log.info("Uploading file {}", file.getName());
            channelSftp.put( file.getAbsolutePath(), remoteDirectory + file.getName() );
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

        String remoteDirectory = report.getSftpHostDirectory();

        if(remoteDirectory.contains("{today}")) {

            LocalDate localDate = LocalDate.now();

            String today = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

            remoteDirectory = remoteDirectory.replace("{today}", today);
        }

        remoteDirectory = "/ftp/" + remoteDirectory;

        return remoteDirectory;
    }

    public void close() {
        channelSftp.exit();
        session.disconnect();
    }
}
