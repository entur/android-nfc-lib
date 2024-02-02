package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class ReaderEndPollingRequestMessage extends NfcMessage {

    public static final int TYPE = 8;

    public ReaderEndPollingRequestMessage() {
        this(nextId());
    }

    public ReaderEndPollingRequestMessage(int id) {
        super(TYPE, id);
    }

}
