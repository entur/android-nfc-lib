package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.readers.Acr1252CardTerminal;

public class ExtendedCardTerminalFactory {

    private final CardTerminalMetadataEnricher enricher;

    public ExtendedCardTerminalFactory(CardTerminalMetadataEnricher enricher) {
        this.enricher = enricher;
    }

    public ExtendedCardTerminal create(CardTerminal cardTerminal) throws CardException {
        String name = cardTerminal.getName();

        if(name.contains("ACR1252")) {
            Acr1252CardTerminal terminal = new Acr1252CardTerminal(cardTerminal);
            System.out.println("Enrich");

            enricher.enrich(terminal);

            System.out.println("Stop polling");

            terminal.stopPolling();

            return terminal;
        }

        throw new RuntimeException("Unsupported reader " + name);
    }
}
