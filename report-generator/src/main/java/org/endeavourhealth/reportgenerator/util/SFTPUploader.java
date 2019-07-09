package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;
import org.endeavourhealth.reportgenerator.model.Report;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.*;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Slf4j
public class SFTPUploader {

    public void upload(Report report) throws Exception {

//        cleanStagingDirectory(report);

//        File zipFile = zipDirectory(report);

        String path = "/home/hal/test.csv";

        File file = new File( path);

        encryptFile(file);

        sftp(report);
    }

    private void sftp(Report report) throws JSchException, IOException {
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
    }

    private void cleanStagingDirectory(Report report) throws IOException {
        File stagingDirectory = new File("/home/hal/staging");

        File[] files = stagingDirectory.listFiles();
        if (files.length > 0) {

            log.info("Staging directory is not empty, deleting files");

            for (File file : files) {
                if (file.isFile()) {
                    log.debug("Deleting the file: {}", file.getName());
                    file.delete();
                }
                if (file.isDirectory()) {
                    log.debug("Deleting the directory: {}", file.getName());
                    FileUtils.deleteDirectory(file);
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {

        if (args == null || args.length != 7) {
            log.error("Invalid parameters.");
            System.exit(-1);
        }

        log.info("");
        log.info("Running Zip, Encrypt and Upload process with the following parameters:");
        log.info("Source Directory  : " + args[0]);
        log.info("Staging Directory : " + args[1]);
        log.info("Hostname          : " + args[2]);
        log.info("Port              : " + args[3]);
        log.info("Username          : " + args[4]);
        log.info("SFTP Location     : " + args[5]);
        log.info("Key File          : " + args[6]);
        log.info("");


//            ConnectionDetails con = new ConnectionDetails();
//            con.setHostname(args[2]);
//            con.setPort(Integer.valueOf(args[3]));
//            con.setUsername(args[4]);
//            try {
//                con.setClientPrivateKey(FileUtils.readFileToString(new File(args[6]), (String) null));
//                con.setClientPrivateKeyPassword("");
//            } catch (IOException e) {
//                log.info("");
//                log.error("Unable to read client private key file." + e.getMessage());
//                log.info("");
//                System.exit(-1);
//            }
//
//            SftpConnection sftp = new SftpConnection(con);
//            try {
//                sftp.open();
//                log.info("");
//                log.info("SFTP connection established");
//                log.info("");
//                sftp.close();
//            } catch (Exception e) {
//                log.info("");
//                log.error("Unable to connect to the SFTP server. " + e.getMessage());
//                log.info("");
//                System.exit(-1);
//            }
//
//            try {
//                ZipEncryptUpload.zipDirectory(source_dir, staging_dir);
//
//            } catch (Exception e) {
//                log.info("");
//                log.error("Unable to create the zip file/s." + e.getMessage());
//                log.info("");
//                System.exit(-1);
//            }
//
//            try {
//                File zipFile = new File(staging_dir.getAbsolutePath() +
//                        File.separator +
//                        source_dir.getName() +
//                        ".zip");
//                if (!ZipEncryptUpload.encryptFile(zipFile)) {
//                    log.info("");
//                    log.error("Unable to encrypt the zip file/s. ");
//                    log.info("");
//                    System.exit(-1);
//                }
//            } catch (Exception e) {
//                log.info("");
//                log.error("Unable to encrypt the zip file/s. " + e.getMessage());
//                log.info("");
//                System.exit(-1);
//            }
//
//            try {
//                sftp.open();
//                String location = args[5];
//                File[] files = staging_dir.listFiles();
//                log.info("");
//                log.info("Starting file/s upload.");
//                for (File file : files) {
//                    log.info("Uploading file:" + file.getName());
//                    sftp.put(file.getAbsolutePath(), location);
//                }
//                log.info("");
//                sftp.close();
//            } catch (Exception e) {
//                log.info("");
//                log.error("Unable to do SFTP operation. " + e.getMessage());
//                log.info("");
//                System.exit(-1);
//            }
//            log.info("");
//            log.info("Process completed.");
//            log.info("");
//            System.exit(0);
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

    private X509Certificate getCertificate() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

        X509Certificate certificate = (X509Certificate)
                certFactory.generateCertificate(SFTPUploader.class.getClassLoader().getResourceAsStream("discovery.cer"));

       return certificate;
    }

    private void encryptFile(File file) throws Exception {

        log.info("Encrypting the file {}", file.getAbsolutePath());

        X509Certificate certificate = getCertificate();

        FileOutputStream output = null;

        try {
            byte[] data = IOUtils.toByteArray(new FileInputStream(file));

            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();

            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator( certificate );

            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);

            CMSTypedData cmsTypedData = new CMSProcessableByteArray(data);

            OutputEncryptor outputEncryptor =
                    new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();

            CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(cmsTypedData, outputEncryptor);

            output = new FileOutputStream(file + ".enc");
            output.write(cmsEnvelopedData.getEncoded());
            output.flush();

            log.info("File encryption was successful.");
        } finally {
            output.close();
        }
    }
}

