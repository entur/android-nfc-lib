package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentResponseMessage;

public class HwbReaderPresentResponseMessage extends ReaderPresentResponseMessage<String> {

    private DiagnosticsSchema diagnostics;

    public HwbReaderPresentResponseMessage(DiagnosticsSchema diagnostics) {
        super(diagnostics.getTraceId().toString(), true);
    }

    public DiagnosticsSchema getPayload() {
        return diagnostics;
    }
}
