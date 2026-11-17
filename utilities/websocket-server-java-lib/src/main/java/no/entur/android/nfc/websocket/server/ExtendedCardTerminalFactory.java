package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.readers.Acr1222LCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr122UCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1251CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1252CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1255UCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1281CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1283CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1552CardTerminal;

public class ExtendedCardTerminalFactory {

    private final CardTerminalMetadataEnricher enricher;

    public ExtendedCardTerminalFactory(CardTerminalMetadataEnricher enricher) {
        this.enricher = enricher;
    }

    public ExtendedCardTerminal create(CardTerminal cardTerminal) throws CardException {
        ExtendedCardTerminal terminal = createTerminal(cardTerminal);
        if(terminal == null) {
            throw new RuntimeException("Unsupported reader " + cardTerminal.getName());
        }

        enricher.enrich(terminal);

        terminal.stopPolling();

        return terminal;
    }

    /**
     * Create a terminal for a reader. Override to support additional readers.
     *
     * @param cardTerminal reader
     * @return terminal, or null if the reader is not supported
     */

    protected ExtendedCardTerminal createTerminal(CardTerminal cardTerminal) {
        String name = cardTerminal.getName();
        if(name == null) {
            return null;
        }

        // check ACR1222 before ACR122(U)
        if(name.contains("ACR1222")) {
            return new Acr1222LCardTerminal(cardTerminal);
        }
        if(name.contains("ACR122")) {
            return new Acr122UCardTerminal(cardTerminal);
        }
        if(name.contains("ACR1251")) {
            return new Acr1251CardTerminal(cardTerminal);
        }
        if(name.contains("ACR1252")) {
            return new Acr1252CardTerminal(cardTerminal);
        }
        if(name.contains("ACR1255")) {
            return new Acr1255UCardTerminal(cardTerminal);
        }
        if(name.contains("ACR1281")) {
            return new Acr1281CardTerminal(cardTerminal);
        }
        if(name.contains("ACR1283")) {
            return new Acr1283CardTerminal(cardTerminal);
        }
        if(name.contains("ACR1552")) {
            return new Acr1552CardTerminal(cardTerminal);
        }
        return null;
    }
}
