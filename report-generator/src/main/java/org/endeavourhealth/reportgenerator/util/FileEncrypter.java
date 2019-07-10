package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.endeavourhealth.reportgenerator.model.Report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

@Slf4j
public class FileEncrypter {

    private final String outputDirectory;

    public FileEncrypter(Properties properties) {

        outputDirectory = properties.getProperty("output.directory");
    }

    public void encryptFile(Report report) throws Exception {

        String path = outputDirectory + File.separator + report.getName();

        File file = new File(path);

        log.info("Encrypting the file {}", file.getAbsolutePath());

        encryptFile( file );
    }

    private void encryptFile(File file) throws Exception {

        X509Certificate certificate = getCertificate();

        byte[] data = IOUtils.toByteArray(new FileInputStream(file));

        CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();

        JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(certificate);

        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);

        CMSTypedData cmsTypedData = new CMSProcessableByteArray(data);

        OutputEncryptor outputEncryptor =
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();

        CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(cmsTypedData, outputEncryptor);

        try (FileOutputStream output = new FileOutputStream(file + ".enc")) {
            output.write(cmsEnvelopedData.getEncoded());
            output.flush();
        }
    }

    private X509Certificate getCertificate() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

        X509Certificate certificate = (X509Certificate)
                certFactory.generateCertificate(FileEncrypter.class.getClassLoader().getResourceAsStream("discovery.cer"));

        return certificate;
    }
}
