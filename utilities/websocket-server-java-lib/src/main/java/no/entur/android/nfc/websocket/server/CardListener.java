package no.entur.android.nfc.websocket.server;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;

interface CardListener {

    void cardConnected(Card card) throws CardException;

    void cardDisconnected(Card card);
}
