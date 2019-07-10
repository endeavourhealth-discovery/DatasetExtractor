package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class SFTPUploader {

    private final String outputDirectory;

    public SFTPUploader(Properties properties) {
        outputDirectory = properties.getProperty("output.directory");
    }

    public void upload(Report report) throws Exception {

        String path = outputDirectory + File.separator + report.getName();

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

        channel.put(file.getAbsolutePath(), "/ftp/" + file.getAbsolutePath());
    }


//    private File zipDirectory(Report report) throws Exception {
//
//        String source = "/hal/media";
//
//        log.info("Compressing contents of: " + source.getAbsolutePath());
//
//        ZipFile zipFile = new ZipFile(staging + File.separator + source.getName() + ".zip");
//
//        log.debug("Creating file: " + zipFile.getFile().getAbsolutePath());
//
//        ZipParameters parameters = new ZipParameters();
//
//        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
//        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
//        parameters.setIncludeRootFolder(false);
//
//        zipFile.createZipFileFromFolder(source, parameters, true, 10485760);
//
//        log.info(staging.listFiles().length + " File/s successfully created.");
//
//        File zipFile = new File(staging_dir.getAbsolutePath() +
//                File.separator +
//                source_dir.getName() +
//                ".zip");
//
//        return zipFile;
//    }

}

