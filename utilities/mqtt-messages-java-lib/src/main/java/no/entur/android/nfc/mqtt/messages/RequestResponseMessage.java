package no.entur.android.nfc.mqtt.messages;

public class RequestResponseMessage<T> {

    // i.e. correlation id, for matching response to request. Not for tracing.
    protected final T correlationId;

    public RequestResponseMessage(T correlationId) {
        this.correlationId = correlationId;
    }

    public T getCorrelationId() {
        return correlationId;
    }
}
