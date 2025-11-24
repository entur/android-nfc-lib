package no.entur.android.nfc.mqtt.messages;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class DefaultJsonResponseMqttMessage<T, P> implements ObjectMqttMessage<P> {

    protected final P payload;

    protected final String topic;

    public DefaultJsonResponseMqttMessage(P payload, String topic) {
        this.payload = payload;
        this.topic = topic;
    }

    @Override
    public P getPayload() {
        return payload;
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
