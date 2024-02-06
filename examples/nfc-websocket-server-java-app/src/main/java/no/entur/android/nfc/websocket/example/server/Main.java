package no.entur.android.nfc.websocket.example.server;

import java.net.UnknownHostException;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import no.entur.android.nfc.websocket.server.CardTerminalsFilter;
import no.entur.android.nfc.websocket.server.CardTerminalsPollingPool;
import no.entur.android.nfc.websocket.server.CardTerminalsPollingServer;
import no.entur.android.nfc.websocket.server.WebSocketNfcServer;

public class Main {

    public static final void main(String[] args) throws UnknownHostException {
        int port = 3000; // 843 flash policy port
        CardTerminalsFilter filter = new CardTerminalsFilter() {
            @Override
            public boolean accept(CardTerminal cardTerminal) {
                return !cardTerminal.getName().contains(" SAM");
            }
        };
        WebSocketNfcServer s = new WebSocketNfcServer(port, filter);
        s.start();
    }
}
