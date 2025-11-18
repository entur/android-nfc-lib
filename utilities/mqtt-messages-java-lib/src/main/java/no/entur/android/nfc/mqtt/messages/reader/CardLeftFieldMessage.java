package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultResponseMessage;

public class CardLeftFieldMessage<T> extends DefaultResponseMessage<T> {

    public CardLeftFieldMessage(T correlationId) {
        super(correlationId);
    }
}
