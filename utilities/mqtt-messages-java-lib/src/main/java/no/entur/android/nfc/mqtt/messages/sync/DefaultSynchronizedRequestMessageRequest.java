package no.entur.android.nfc.mqtt.messages.sync;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class DefaultSynchronizedRequestMessageRequest<T, P> implements SynchronizedRequestMessageRequest<T> {

    // i.e. correlation id, for matching response to request. Not for tracing.
    protected final T correlationId;
    protected final P payload;
    protected final String topic;

    public DefaultSynchronizedRequestMessageRequest(T correlationId, P payload, String topic) {
        this.correlationId = correlationId;
        this.payload = payload;
        this.topic = topic;
    }

    public T getCorrelationId() {
        return correlationId;
    }

    @Override
    public P getPayload() {
        return payload;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        if(payload != null) {
            generator.writeObject(payload);
        } else {
            // empty
            generator.writeStartObject();
            generator.writeEndObject();
        }
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
