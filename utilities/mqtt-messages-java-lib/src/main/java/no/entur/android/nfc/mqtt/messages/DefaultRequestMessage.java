package no.entur.android.nfc.mqtt.messages;

public class DefaultRequestMessage<T> implements RequestMessage<T> {

    // i.e. correlation id, for matching response to request. Not for tracing.
    protected final T correlationId;

    public DefaultRequestMessage(T correlationId) {
        this.correlationId = correlationId;
    }

    public T getCorrelationId() {
        return correlationId;
    }
}
