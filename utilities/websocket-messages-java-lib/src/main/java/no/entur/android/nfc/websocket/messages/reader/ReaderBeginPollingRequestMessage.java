package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class ReaderBeginPollingRequestMessage extends NfcMessage {

    public static final int TYPE = 6;

    public ReaderBeginPollingRequestMessage() {
        this(nextId());
    }

    public ReaderBeginPollingRequestMessage(int id) {
        super(TYPE, id);
    }

}
