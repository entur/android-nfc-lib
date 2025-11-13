package no.entur.android.nfc.mqtt.messages.card;

import no.entur.android.nfc.mqtt.messages.RequestResponseMessage;

public class CardRequestResponseMessage<T> extends RequestResponseMessage<T> {

    public CardRequestResponseMessage(T correlationId) {
        super(correlationId);
    }
}
