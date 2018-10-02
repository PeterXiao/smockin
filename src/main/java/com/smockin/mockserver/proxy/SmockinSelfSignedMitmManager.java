package com.smockin.mockserver.proxy;

import com.smockin.utils.GeneralUtils;
import io.netty.handler.codec.http.HttpRequest;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.littleshoot.proxy.MitmManager;

public class SmockinSelfSignedMitmManager implements MitmManager {

    private SmockinSelfSignedSslEngineSource selfSignedSslEngineSource;

    public SmockinSelfSignedMitmManager() {
        selfSignedSslEngineSource = GeneralUtils.retrieveSelfSignedSSLCert("proxy", "smockinproxy");
    }

    public SSLEngine serverSslEngine(String peerHost, int peerPort) {
        return this.selfSignedSslEngineSource.newSslEngine(peerHost, peerPort);
    }

    public SSLEngine serverSslEngine() {
        return this.selfSignedSslEngineSource.newSslEngine();
    }

    public SSLEngine clientSslEngineFor(HttpRequest httpRequest, SSLSession serverSslSession) {
        return this.selfSignedSslEngineSource.newSslEngine();
    }

}
