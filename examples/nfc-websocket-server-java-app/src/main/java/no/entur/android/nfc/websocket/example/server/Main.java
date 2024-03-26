package no.entur.android.nfc.websocket.example.server;

import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.CardTerminalsFilter;
import no.entur.android.nfc.websocket.server.DefaultCardTerminalMetadataEnricher;
import no.entur.android.nfc.websocket.server.ExtendedCardTerminalFactory;
import no.entur.android.nfc.websocket.server.WebSocketNfcServer;

public class Main {

    public static final void main(String[] args) throws Exception {
        int port = 3001; // 843 flash policy port
        CardTerminalsFilter filter = new CardTerminalsFilter() {
            @Override
            public boolean accept(CardTerminal cardTerminal) {
                return !cardTerminal.getName().contains(" SAM");
            }
        };

        ExtendedCardTerminalFactory extendedCardTerminalFactory = new ExtendedCardTerminalFactory(new DefaultCardTerminalMetadataEnricher());


        WebSocketNfcServer s = new WebSocketNfcServer(port, filter, extendedCardTerminalFactory);
        s.start();
    }
}
