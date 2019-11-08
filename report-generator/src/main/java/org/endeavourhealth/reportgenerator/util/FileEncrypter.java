package org.endeavourhealth.reportgenerator.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Slf4j
public class FileEncrypter {


    public void encryptDirectory(File directory) throws Exception {

      for (File file : directory.listFiles()) {
        encryptFile( file );
      }
    }

    public void encryptFile(File file) throws Exception {

        log.info("Encrypting {}", file);

        X509Certificate certificate = getCertificate();

        byte[] data = IOUtils.toByteArray(new FileInputStream(file));

        CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();

        JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(certificate);

        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);

        CMSTypedData cmsTypedData = new CMSProcessableByteArray(data);

        OutputEncryptor outputEncryptor =
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();

        CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(cmsTypedData, outputEncryptor);

        try (FileOutputStream output = new FileOutputStream(file)) {
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
