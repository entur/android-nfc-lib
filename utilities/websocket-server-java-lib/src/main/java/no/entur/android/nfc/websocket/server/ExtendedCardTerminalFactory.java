package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.readers.Acr1252CardTerminal;

public class ExtendedCardTerminalFactory {

    public ExtendedCardTerminal create(CardTerminal cardTerminal) throws CardException {
        String name = cardTerminal.getName();

        if(name.contains("ACR1252")) {
            Acr1252CardTerminal terminal = new Acr1252CardTerminal(cardTerminal);

            terminal.stopPolling();

            return terminal;
        }

        throw new RuntimeException("Unsupported reader " + name);
    }
}
