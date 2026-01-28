package no.entur.android.nfc.external.hid.reader;

import no.entur.android.nfc.external.hid.dto.atr210.AbstractMessage;
import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedResponseMessage;

public class Atr210FirstMessageOnTopicSynchronizedResponseMessage<T extends AbstractMessage> extends DefaultSynchronizedResponseMessage<String> {

    private T response;

    public Atr210FirstMessageOnTopicSynchronizedResponseMessage(T response, String topic) {
        super(topic);

        this.response = response;
    }

    public T getPayload() {
        return response;
    }
}
