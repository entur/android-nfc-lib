package no.entur.android.nfc.mqtt.messages.card;

import no.entur.android.nfc.mqtt.messages.RequestResponseMessage;

public class CardAdpuResponseMessage<T> extends CardRequestResponseMessage<T> {

    private final byte[] adpu;

    public CardAdpuResponseMessage(byte[] adpu, T id) {
        super(id);
        this.adpu = adpu;
    }

    public byte[] getAdpu() {
        return adpu;
    }
}
