package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.DefaultResponseMessage;

public class ReaderPresentResponseMessage<T> extends DefaultResponseMessage<T> {


    protected final boolean present;

    public ReaderPresentResponseMessage(T correlationId, boolean present) {
        super(correlationId);
        this.present = present;
    }

    public boolean isPresent() {
        return present;
    }
}