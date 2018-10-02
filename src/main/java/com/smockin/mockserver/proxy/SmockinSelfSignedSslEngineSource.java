package com.smockin.mockserver.proxy;

import com.google.common.io.ByteStreams;
import org.littleshoot.proxy.SslEngineSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class SmockinSelfSignedSslEngineSource implements SslEngineSource {

    private static final Logger logger = LoggerFactory.getLogger(SmockinSelfSignedSslEngineSource.class);

    public final String PASSWORD = "Be Your Own Lantern";
    private final String keyStoreName;
    private final File keyStoreFile;
    private final File trustStoreFile;
    private final boolean trustAllServers;
    private final boolean sendCerts;
    private SSLContext sslContext;

    public SmockinSelfSignedSslEngineSource(String keyStoreDir, String keyStoreName, boolean trustAllServers, boolean sendCerts) {
        this.trustAllServers = trustAllServers;
        this.sendCerts = sendCerts;
        this.keyStoreName = keyStoreName;
        this.keyStoreFile = new File(keyStoreDir + File.separator + keyStoreName + "_keystore.jks");
        this.trustStoreFile = new File(keyStoreDir + File.separator + keyStoreName + "_cert");
        this.initializeKeyStore();
        this.initializeSSLContext();
    }

    public SSLEngine newSslEngine() {
        return this.sslContext.createSSLEngine();
    }

    public SSLEngine newSslEngine(String peerHost, int peerPort) {
        return this.sslContext.createSSLEngine(peerHost, peerPort);
    }

    public File getKeyStoreFile() {
        return keyStoreFile;
    }

    private void initializeKeyStore() {

        if (this.keyStoreFile.isFile()) {
            logger.info("Not deleting keystore");
            return;
        }

        this.nativeCall("keytool", "-genkey", "-alias", keyStoreName, "-keysize", "4096", "-validity", "36500", "-keyalg", "RSA", "-dname", "CN=" + keyStoreName, "-keypass", PASSWORD, "-storepass", PASSWORD, "-keystore", this.keyStoreFile.getAbsolutePath());
        this.nativeCall("keytool", "-exportcert", "-alias", keyStoreName, "-keystore", this.keyStoreFile.getAbsolutePath(), "-storepass", PASSWORD, "-file", trustStoreFile.getAbsolutePath());
    }

    private void initializeSSLContext() {

        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");

        if (algorithm == null) {
            algorithm = "SunX509";
        }

        try {

            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(this.keyStoreFile), PASSWORD.toCharArray());

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, PASSWORD.toCharArray());

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(ks);

            final TrustManager[] trustManagers;

            if (!this.trustAllServers) {
                trustManagers = tmf.getTrustManagers();
            } else {
                trustManagers = new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }};
            }

            final KeyManager[] keyManagers = (this.sendCerts) ? kmf.getKeyManagers() : new KeyManager[0];

            this.sslContext = SSLContext.getInstance("TLS");
            this.sslContext.init(keyManagers, trustManagers, null);

        } catch (Exception var7) {
            throw new Error("Failed to initialize the server-side SSLContext", var7);
        }
    }

    private String nativeCall(String... commands) {
        if (logger.isInfoEnabled())
            logger.info("Running '{}'", Arrays.asList(commands));

        final ProcessBuilder pb = new ProcessBuilder(commands);

        try {
            final Process process = pb.start();
            final InputStream is = process.getInputStream();
            byte[] data = ByteStreams.toByteArray(is);
            final String dataAsString = new String(data);

            if (logger.isInfoEnabled())
                logger.info("Completed native call: '{}'\nResponse: '" + dataAsString + "'", Arrays.asList(commands));

            return dataAsString;
        } catch (IOException var7) {
            logger.error("Error running commands: " + Arrays.asList(commands), var7);
            return "";
        }
    }
}
