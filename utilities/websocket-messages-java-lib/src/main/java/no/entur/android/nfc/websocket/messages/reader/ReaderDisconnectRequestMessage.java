package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class ReaderDisconnectRequestMessage extends NfcMessage {

    public static final int TYPE = 3;

    public ReaderDisconnectRequestMessage() {
        this(nextId());
    }

    public ReaderDisconnectRequestMessage(int id) {
        super(TYPE, id);
    }

}
