package no.entur.android.nfc.mqtt.messages.sync;

public interface SynchronizedResponseMessage<T> {

    T getCorrelationId();
}
