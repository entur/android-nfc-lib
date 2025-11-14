package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultRequestMessage;

public class CardAdpuRequestMessage<T> extends DefaultRequestMessage<T> {

    private final byte[] adpu;

	public CardAdpuRequestMessage(T id, byte[] adpu) {
        super(id);
        this.adpu = adpu;
	}

    public byte[] getAdpu() {
		return adpu;
	}

}
