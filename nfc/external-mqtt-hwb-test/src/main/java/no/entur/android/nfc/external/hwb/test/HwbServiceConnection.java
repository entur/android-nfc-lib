package no.entur.android.nfc.external.hwb.test;

import java.io.Closeable;
import no.entur.android.nfc.external.hwb.HwbMqttService;

public class HwbServiceConnection implements Closeable {

    private final HwbServiceConnector connector;
    private final boolean stopServiceOnClose;

    private final HwbMqttService service;

    public HwbServiceConnection(HwbServiceConnector connector, HwbMqttService service, boolean stopServiceOnClose) {
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

    public HwbMqttService getService() {
        return service;
    }
}
