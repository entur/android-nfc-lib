package no.entur.android.nfc.mqtt.messages.sync;

import no.entur.android.nfc.mqtt.messages.JsonRequestMqttMessage;

public interface SynchronizedRequestMessageRequest<T> extends JsonRequestMqttMessage {

    T getCorrelationId();

    /**
     * Check whether a response with the same correlation id is the response to this request.
     *
     * @param response response with the same correlation id
     * @return true if the response belongs to this request, false if it should be ignored (i.e. a late response to an earlier request)
     */

    default boolean isResponse(SynchronizedResponseMessage<T> response) {
        return true;
    }
}
