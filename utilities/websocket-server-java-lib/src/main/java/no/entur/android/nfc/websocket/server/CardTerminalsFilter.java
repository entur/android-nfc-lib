package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardTerminal;

public interface CardTerminalsFilter {

    boolean accept(CardTerminal cardTerminal);

}
