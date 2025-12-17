package no.entur.android.nfc.external.hid.reader;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedRequestMessageRequest;

public class Atr210FirstMessageOnTopicSynchronizedRequestMessage<P> extends DefaultSynchronizedRequestMessageRequest<String, P> {

    public Atr210FirstMessageOnTopicSynchronizedRequestMessage(P payload, String requestTopic, String responseTopic) {
        super(responseTopic, payload, requestTopic);
    }
}
