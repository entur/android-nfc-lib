package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.sync.DefaultSynchronizedResponseMessage;

public class ReaderPresentResponseMessage<T> extends DefaultSynchronizedResponseMessage<T> {

    protected final boolean present;

    public ReaderPresentResponseMessage(T correlationId, boolean present) {
        super(correlationId);
        this.present = present;
    }


    public boolean isPresent() {
        return present;
    }
}