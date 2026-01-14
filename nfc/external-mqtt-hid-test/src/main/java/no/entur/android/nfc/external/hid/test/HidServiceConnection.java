package no.entur.android.nfc.external.hid.test;

import java.io.Closeable;

import no.entur.android.nfc.external.hid.HidMqttService;

public class HidServiceConnection implements Closeable {

    private final HidServiceConnector connector;
    private final boolean stopServiceOnClose;

    private final HidMqttService service;

    public HidServiceConnection(HidServiceConnector connector, HidMqttService service, boolean stopServiceOnClose) {
        this.connector = connector;
        this.service = service;
        this.stopServiceOnClose = stopServiceOnClose;
    }

    public void close() {
        connector.unbind();
        if(stopServiceOnClose) {
            connector.stop();
        }
    }

    public HidMqttService getService() {
        return service;
    }
}
