package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultRequestMessage;

public class ReaderPresentRequestMessage<T> extends DefaultRequestMessage<T> {

    public ReaderPresentRequestMessage(T correlationId) {
        super(correlationId);
    }

}
