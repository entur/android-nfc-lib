package no.entur.android.nfc.external.hwb.reader;

import java.util.UUID;

import no.entur.android.nfc.mqtt.messages.reader.ReaderContext;

public class HwbReaderContext implements ReaderContext {

    protected String deviceId;

    protected String name;

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }
}
