package com.smockin.mockserver.dto;

/**
 * Created by mgallina.
 */
public class MockServerState {

    private boolean running;
    private int port;
    private boolean secure;

    public MockServerState() {
    }

    public MockServerState(boolean running, int port, boolean secure) {
        this.running = running;
        this.port = port;
        this.secure = secure;
    }

    public boolean isRunning() {
        return running;
    }
    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSecure() {
        return secure;
    }
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

}
