package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

interface CardTerminalsPollingListener {

    void connected(CardTerminal cardTerminal) throws CardException;

    void disconnected(CardTerminal cardTerminal);
}
