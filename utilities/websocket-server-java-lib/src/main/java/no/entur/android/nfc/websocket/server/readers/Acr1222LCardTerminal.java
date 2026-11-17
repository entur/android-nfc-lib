package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1222L. There is no automatic polling setting, so polling is stopped by not polling for any card types.
 */

public class Acr1222LCardTerminal extends AcsExtendedCardTerminal {

    private static final int PICC_MASK = POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B;

    public Acr1222LCardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingEscapeCommand();
    }

    @Override
    public void startPolling() throws CardException {
        setPICCUsingEscapeCommand(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B, PICC_MASK);
    }

    @Override
    public void stopPolling() throws CardException {
        setPICCUsingEscapeCommand(0, PICC_MASK);
    }
}
