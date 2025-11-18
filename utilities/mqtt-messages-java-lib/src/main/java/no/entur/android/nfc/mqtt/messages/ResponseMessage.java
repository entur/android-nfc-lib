package no.entur.android.nfc.mqtt.messages;

public interface ResponseMessage<T> {

    T getCorrelationId();
}
