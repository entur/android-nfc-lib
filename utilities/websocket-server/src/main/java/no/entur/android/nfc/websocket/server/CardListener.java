package no.entur.android.nfc.websocket.server;

import javax.smartcardio.Card;

interface CardListener {

    void cardConnected(Card card);

    void cardDisconnected(Card card);
}
