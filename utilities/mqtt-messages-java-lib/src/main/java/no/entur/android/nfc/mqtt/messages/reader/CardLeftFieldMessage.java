package no.entur.android.nfc.mqtt.messages.reader;

public class CardLeftFieldMessage<D>  {

    private D deviceId;

    public D getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(D deviceId) {
        this.deviceId = deviceId;
    }
}
