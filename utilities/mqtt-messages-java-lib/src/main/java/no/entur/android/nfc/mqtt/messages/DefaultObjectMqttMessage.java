package no.entur.android.nfc.mqtt.messages;

public class DefaultObjectMqttMessage<T, P> implements ObjectMqttMessage<P> {

    protected final P payload;

    protected final String topic;

    public DefaultObjectMqttMessage(P payload, String topic) {
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
