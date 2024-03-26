package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;

public class ReaderBeginPollingResponseMessage extends NfcStatusResponseMessage {

    public static final int TYPE = 5;

    public ReaderBeginPollingResponseMessage(int id) {
        super(TYPE, id);
    }

    public ReaderBeginPollingResponseMessage(int id, int status) {
        super(TYPE, id);
        this.status = status;
    }


}
