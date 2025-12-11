package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentResponseMessage;

public class Atr210ReaderPresentSynchronizedResponseMessage extends ReaderPresentResponseMessage {

    private DiagnosticsSchema diagnostics;

    public Atr210ReaderPresentSynchronizedResponseMessage(DiagnosticsSchema diagnostics, boolean present) {
        super(diagnostics.getDeviceId(), present);

        this.diagnostics = diagnostics;
    }

    public DiagnosticsSchema getPayload() {
        return diagnostics;
    }
}
