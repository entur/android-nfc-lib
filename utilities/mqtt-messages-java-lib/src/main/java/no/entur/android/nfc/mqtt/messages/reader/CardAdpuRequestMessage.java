package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultRequestMessage;

public abstract class CardAdpuRequestMessage<T> extends DefaultRequestMessage<T> {

	public CardAdpuRequestMessage(T id) {
        super(id);
	}

}
