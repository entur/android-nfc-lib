package no.entur.android.nfc.websocket.messages.card.broadcast;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class CardLostMessage extends NfcMessage {

    public static final int TYPE = 100;


    public CardLostMessage() {
        super(TYPE, nextId());
    }

    public CardLostMessage(int id) {
        super(TYPE, id);
    }

}
