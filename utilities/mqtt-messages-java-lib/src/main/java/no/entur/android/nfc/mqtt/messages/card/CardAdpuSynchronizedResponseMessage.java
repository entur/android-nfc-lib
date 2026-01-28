package no.entur.android.nfc.mqtt.messages.card;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedResponseMessage;

public abstract class CardAdpuSynchronizedResponseMessage<T> extends DefaultSynchronizedResponseMessage<T> {

    public CardAdpuSynchronizedResponseMessage(T id) {
        super(id);
    }

    public abstract byte[] getAdpu() throws IOException;

}
