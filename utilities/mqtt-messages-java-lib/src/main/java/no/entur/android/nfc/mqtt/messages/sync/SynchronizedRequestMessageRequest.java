package no.entur.android.nfc.mqtt.messages.sync;

import no.entur.android.nfc.mqtt.messages.JsonRequestMqttMessage;

public interface SynchronizedRequestMessageRequest<T> extends JsonRequestMqttMessage {

    T getCorrelationId();
}
