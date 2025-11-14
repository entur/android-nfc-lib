package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultResponseMessage;

public class CardAdpuResponseMessage<T> extends DefaultResponseMessage<T> {

    private final byte[] adpu;

    public CardAdpuResponseMessage(byte[] adpu, T id) {
        super(id);
        this.adpu = adpu;
    }

    public byte[] getAdpu() {
        return adpu;
    }
}
