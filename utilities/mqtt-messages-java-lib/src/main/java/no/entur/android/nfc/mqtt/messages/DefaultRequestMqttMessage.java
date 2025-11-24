package no.entur.android.nfc.mqtt.messages;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class DefaultRequestMqttMessage<T, P> implements JsonRequestMqttMessage<P> {

    protected final P payload;

    protected final String topic;

    public DefaultRequestMqttMessage(P payload, String topic) {
        this.payload = payload;
        this.topic = topic;
    }

    @Override
    public P getPayload() {
        return payload;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        generator.writeObject(payload);
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
