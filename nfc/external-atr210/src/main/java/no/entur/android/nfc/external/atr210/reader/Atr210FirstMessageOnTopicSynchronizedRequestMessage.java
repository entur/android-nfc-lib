package no.entur.android.nfc.external.atr210.reader;

import no.entur.android.nfc.external.atr210.schema.AbstractMessage;
import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedResponseMessage;

public class Atr210FirstMessageOnTopicSynchronizedRequestMessage<P> extends DefaultSynchronizedRequestMessageRequest<String, P> {

    public Atr210FirstMessageOnTopicSynchronizedRequestMessage(P payload, String requestTopic, String responseTopic) {
        super(responseTopic, payload, requestTopic);
    }
}
