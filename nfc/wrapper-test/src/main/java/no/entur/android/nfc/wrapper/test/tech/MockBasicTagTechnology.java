package no.entur.android.nfc.wrapper.test.tech;

import java.io.IOException;

/**
 * A base class for tag technologies that are built on top of transceive().
 */
public abstract class MockBasicTagTechnology {

    protected boolean connected;
    protected int technology;

    public MockBasicTagTechnology(int tech) {
        technology = tech;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect() throws IOException {
        connected = true;
    }

    public void reconnect() throws IOException {
        if (!connected) {
            throw new IllegalStateException("Technology not connected yet");
        }
    }

    public void close() throws IOException {
        connected = false;
    }

    public int getTechnology() {
        return technology;
    }
}
