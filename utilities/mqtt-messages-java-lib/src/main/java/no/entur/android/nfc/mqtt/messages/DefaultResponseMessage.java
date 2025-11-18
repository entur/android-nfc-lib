package no.entur.android.nfc.mqtt.messages;

public class DefaultResponseMessage<T> implements ResponseMessage<T> {

    // i.e. correlation id, for matching response to request. Not for tracing.
    protected final T correlationId;

    public DefaultResponseMessage(T correlationId) {
        this.correlationId = correlationId;
    }

    public T getCorrelationId() {
        return correlationId;
    }
}
