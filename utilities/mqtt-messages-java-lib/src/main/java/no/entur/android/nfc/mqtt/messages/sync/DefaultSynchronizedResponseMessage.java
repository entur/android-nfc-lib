package no.entur.android.nfc.mqtt.messages.sync;

public class DefaultSynchronizedResponseMessage<T> implements SynchronizedResponseMessage<T> {

    // i.e. correlation id, for matching response to request. Not for tracing.
    protected final T correlationId;

    public DefaultSynchronizedResponseMessage(T correlationId) {
        this.correlationId = correlationId;
    }

    public T getCorrelationId() {
        return correlationId;
    }
}
