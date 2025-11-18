package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DeviceMessage;

public class CardEnteredFieldMessage<D> implements DeviceMessage<D> {

    private D deviceId;

    public D getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(D deviceId) {
        this.deviceId = deviceId;
    }

}
