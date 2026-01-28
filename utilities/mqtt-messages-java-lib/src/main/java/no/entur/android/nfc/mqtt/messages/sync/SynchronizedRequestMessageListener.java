package no.entur.android.nfc.mqtt.messages.sync;

import java.io.IOException;

public interface SynchronizedRequestMessageListener<T> {

    void onRequestMessage(SynchronizedRequestMessageRequest<T> message, SynchronizedResponseMessageListener<T> listener) throws IOException;

}
