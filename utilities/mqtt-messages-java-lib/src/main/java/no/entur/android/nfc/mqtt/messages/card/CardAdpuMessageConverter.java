package no.entur.android.nfc.mqtt.messages.card;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public interface CardAdpuMessageConverter<T, C extends CardContext> {

    <P> CardAdpuSynchronizedRequestMessageRequest<T, P> createCardAdpuRequestMessage(byte[] message, C context);

    CardAdpuSynchronizedResponseMessage createCardAdpuResponseMessage(SynchronizedResponseMessage<T> message, C context);

}
