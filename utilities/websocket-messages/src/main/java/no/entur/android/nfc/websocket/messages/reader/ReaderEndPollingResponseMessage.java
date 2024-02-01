package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;

public class ReaderEndPollingResponseMessage extends NfcStatusResponseMessage {

    public static final int TYPE = 7;

    public ReaderEndPollingResponseMessage() {
        super(TYPE);
    }
}
