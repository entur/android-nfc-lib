package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.mqtt.messages.reader.ReaderContext;

public class Atr210ReaderContext implements ReaderContext {

    protected String deviceId;

    protected DiagnosticsSchema diagnosticsSchema;

    public DiagnosticsSchema getDiagnosticsSchema() {
        return diagnosticsSchema;
    }

    public void setDiagnosticsSchema(DiagnosticsSchema diagnosticsSchema) {
        this.diagnosticsSchema = diagnosticsSchema;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
