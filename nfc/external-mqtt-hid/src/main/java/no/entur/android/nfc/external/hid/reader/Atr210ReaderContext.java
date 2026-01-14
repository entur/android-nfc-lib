package no.entur.android.nfc.external.hid.reader;

import no.entur.android.nfc.external.hid.dto.atr210.ClientIdToProviderIdConverter;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
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
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderId() {
        return providerId;
    }
}
