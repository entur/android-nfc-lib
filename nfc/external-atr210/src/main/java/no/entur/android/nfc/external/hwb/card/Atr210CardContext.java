package no.entur.android.nfc.external.hwb.card;

import java.util.List;
import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.mqtt.messages.card.CardContext;

public class Atr210CardContext implements CardContext  {

    private String deviceId;

    private List<String> technologies;
    private byte[] atr;
    private byte[] historicalBytes;
    private byte[] uid;

    private boolean closed = false;

    private long transcieveTimeout = 687;

    private UUID traceId;

    public Atr210CardContext() {
    }

    @Override
    public byte[] getAtr() {
        return atr;
    }

    @Override
    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }

    @Override
    public byte[] getUid() {
        return uid;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setTranscieveTimeout(long transcieveTimeout) {
        this.transcieveTimeout = transcieveTimeout;
    }

    public long getTranscieveTimeout() {
        return transcieveTimeout;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setTechnologies(List<String> technologies) {
        this.technologies = technologies;
    }

    public void setAtr(byte[] atr) {
        this.atr = atr;
    }

    public void setHistoricalBytes(byte[] historicalBytes) {
        this.historicalBytes = historicalBytes;
    }

    public void setUid(byte[] uid) {
        this.uid = uid;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public UUID getTraceId() {
        return traceId;
    }
}
