package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.hwb.schema.ClientIdToProviderIdConverter;
import no.entur.android.nfc.external.hwb.schema.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.mqtt.messages.reader.ReaderContext;

public class Atr210ReaderContext implements ReaderContext {

    protected String clientId;
    protected String providerId;

    protected HeartbeatResponse heartbeat;

    public HeartbeatResponse getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(HeartbeatResponse heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;

        this.providerId = ClientIdToProviderIdConverter.convert(clientId);
    }

    public String getProviderId() {
        return providerId;
    }
}
