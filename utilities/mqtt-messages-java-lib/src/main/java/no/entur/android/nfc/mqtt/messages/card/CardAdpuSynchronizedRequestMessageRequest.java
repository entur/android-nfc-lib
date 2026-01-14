package no.entur.android.nfc.mqtt.messages.card;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedRequestMessageRequest;

public abstract class CardAdpuSynchronizedRequestMessageRequest<T, P> extends DefaultSynchronizedRequestMessageRequest<T, P> {

	public CardAdpuSynchronizedRequestMessageRequest(T correlationId, P payload, String topic) {
		super(correlationId, payload, topic);
	}
}
