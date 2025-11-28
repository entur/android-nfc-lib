package no.entur.android.nfc.external.hwb.reader;

import java.util.UUID;

import no.entur.android.nfc.mqtt.messages.reader.ReaderContext;

public class HwbReaderContext implements ReaderContext {

    // TODO add diagnostics message here?

    protected String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
