package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardTerminal;

interface CardTerminalsPollingListener {

    void connected(CardTerminal cardTerminal);

    void disconnected(CardTerminal cardTerminal);
}
