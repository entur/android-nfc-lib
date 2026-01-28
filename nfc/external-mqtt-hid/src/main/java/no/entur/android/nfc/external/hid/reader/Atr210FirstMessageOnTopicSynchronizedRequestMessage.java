package no.entur.android.nfc.external.hid.reader;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedRequestMessageRequest;

public class Atr210FirstMessageOnTopicSynchronizedRequestMessage<P> extends DefaultSynchronizedRequestMessageRequest<String, P> {

    /**
     * Empty object
     */

    public Atr210FirstMessageOnTopicSynchronizedRequestMessage(String requestTopic, String responseTopic) {
        super(responseTopic, null, requestTopic);
    }

    public Atr210FirstMessageOnTopicSynchronizedRequestMessage(P payload, String requestTopic, String responseTopic) {
        super(responseTopic, payload, requestTopic);
    }
}
