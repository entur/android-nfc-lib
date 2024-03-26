package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardTerminal;

public class NoopCardTerminalsFilter implements CardTerminalsFilter {
    @Override
    public boolean accept(CardTerminal cardTerminal) {
        return true;
    }
}
