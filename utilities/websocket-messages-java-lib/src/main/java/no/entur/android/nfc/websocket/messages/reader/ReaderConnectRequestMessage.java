package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class ReaderConnectRequestMessage extends NfcMessage {

    public static final int TYPE = 2;

    public ReaderConnectRequestMessage() {
        this(nextId());
    }

    public ReaderConnectRequestMessage(int id) {
        super(TYPE, id);
    }


}
