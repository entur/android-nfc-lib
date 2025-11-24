package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.card.CardContext;

public class CardEnteredFieldMessage<D, CC extends CardContext> {

    private CC cardContext;

    private D deviceId;

    public D getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(D deviceId) {
        this.deviceId = deviceId;
    }

    public CC getCardContext() {
        return cardContext;
    }

    public void setCardContext(CC cardContext) {
        this.cardContext = cardContext;
    }
}
