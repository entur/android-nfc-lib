package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1283L.
 */

public class Acr1283CardTerminal extends AcsExtendedCardTerminal {

    public Acr1283CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingEscapeCommand();
    }

    @Override
    public void startPolling() throws CardException {
        setPICCUsingEscapeCommand(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B, 0xFF);
        // do not enforce ISO14443-4, so that i.e. MIFARE Ultralight cards are also detected
        setAutomaticPICCPolling(AUTOMATIC_PICC_POLLING);
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0);
    }
}
