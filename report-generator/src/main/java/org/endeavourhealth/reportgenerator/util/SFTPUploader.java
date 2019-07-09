package org.endeavourhealth.reportgenerator.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.*;
import java.security.Security;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

@Slf4j
public class SFTPUploader {

    private final String outputDirectory;

    public SFTPUploader(Properties properties) {
        outputDirectory = properties.getProperty("output.directory");
    }

    public void upload(Report report) throws Exception {

//        cleanStagingDirectory(report);

//        File zipFile = zipDirectory(report);

        String path = outputDirectory + File.separator + report.getName();

        File file = new File( path);

        encryptFile(file);

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

        channel.put(file.getAbsolutePath(), "/ftp/test.csv");
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

