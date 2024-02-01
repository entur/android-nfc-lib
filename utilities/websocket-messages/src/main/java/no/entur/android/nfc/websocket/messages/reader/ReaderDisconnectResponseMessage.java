package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;

public class ReaderDisconnectResponseMessage extends NfcStatusResponseMessage {

    public static final int TYPE = 4;

    public ReaderDisconnectResponseMessage() {
        super(TYPE);
    }
}
