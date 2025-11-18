package no.entur.android.nfc.mqtt.messages.reader;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.DefaultResponseMessage;

public abstract class CardAdpuResponseMessage<T> extends DefaultResponseMessage<T> {

    public CardAdpuResponseMessage(T id) {
        super(id);
    }

    public abstract byte[] getAdpu() throws IOException;

}
