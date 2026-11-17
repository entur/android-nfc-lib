package no.entur.android.nfc.websocket.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern PCSC_LITE_SLOT = Pattern.compile(".* \\d{2} (\\d{2})$");

    // pcsc-lite reader names end with the reader index and the slot number
    private static boolean isNonZeroSlot(String name) {
        return isOtherSlot(name, "00");
    }

    private static boolean isOtherSlot(String name, String slot) {
        Matcher matcher = PCSC_LITE_SLOT.matcher(name);
        return matcher.matches() && !matcher.group(1).equals(slot);
    }

    protected ExtendedCardTerminal createTerminal(CardTerminal cardTerminal) {
        String name = cardTerminal.getName();
        if(name == null) {
            return null;
        }

        // check ACR1222 before ACR122(U)
        if(name.contains("ACR1222")) {
            if(isNonZeroSlot(name)) {
                // SAM slots, which pcsc-lite names like the PICC slot (i.e. ACS ACR1222 3S PICC Reader 00 01)
                return null;
            }
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
            if(isOtherSlot(name, "01")) {
                // contact (00) and SAM (02) slots of the dual reader, i.e. ACS ACR1281 1S Dual Reader 00 00
                return null;
            }
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
