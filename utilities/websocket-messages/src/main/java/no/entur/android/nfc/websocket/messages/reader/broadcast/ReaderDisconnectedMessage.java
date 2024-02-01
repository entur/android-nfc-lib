package no.entur.android.nfc.websocket.messages.reader.broadcast;

import no.entur.android.nfc.websocket.messages.NfcMessage;

/**
 *
 * Broadcast message
 *
 */

public class ReaderDisconnectedMessage extends NfcMessage {

    public static final int TYPE = 9;

    public ReaderDisconnectedMessage() {
        super(TYPE);
    }
}
