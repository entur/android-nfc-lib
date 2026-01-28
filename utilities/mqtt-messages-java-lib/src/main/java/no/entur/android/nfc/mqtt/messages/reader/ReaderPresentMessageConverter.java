package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public interface ReaderPresentMessageConverter<T, C> {

    <P> ReaderPresentSynchronizedRequestMessageRequest<T, P> createReaderPresentRequestMessage(C context);

    ReaderPresentResponseMessage createReaderPresentResponseMessage(SynchronizedResponseMessage<T> message, C context);

}
