package no.entur.android.nfc.external.hid.card;

import java.util.List;

import no.entur.android.nfc.mqtt.messages.card.CardContext;

public class Atr210CardContext implements CardContext  {

    private String clientId;

    private String providerId;

    private List<String> technologies;
    private byte[] atr;
    private byte[] historicalBytes;
    private byte[] uid;

    private boolean closed = false;

    private long transcieveTimeout = 687;

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

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getClientId() {
        return clientId;
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

    public void setClientId(String deviceId) {
        this.clientId = deviceId;
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

}
