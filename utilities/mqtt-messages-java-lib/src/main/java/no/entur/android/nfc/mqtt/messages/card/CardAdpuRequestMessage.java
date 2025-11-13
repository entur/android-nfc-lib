package no.entur.android.nfc.mqtt.messages.card;

import no.entur.android.nfc.mqtt.messages.RequestResponseMessage;

public class CardAdpuRequestMessage<T> extends CardRequestResponseMessage<T> {

    private final byte[] adpu;

	public CardAdpuRequestMessage(T id, byte[] adpu) {
        super(id);
        this.adpu = adpu;
	}

    public byte[] getAdpu() {
		return adpu;
	}

}
