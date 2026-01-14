package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedRequestMessageRequest;

public class ReaderPresentSynchronizedRequestMessageRequest<T, P> extends DefaultSynchronizedRequestMessageRequest<T, P> {

    public ReaderPresentSynchronizedRequestMessageRequest(T correlationId, P payload, String topic) {
        super(correlationId, payload, topic);
    }
}
