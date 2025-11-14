package no.entur.android.nfc.mqtt.messages;

public interface RequestMessage<T> {

    T getCorrelationId();
}
