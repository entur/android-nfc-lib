package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1255U-J1 (USB).
 */

public class Acr1255UCardTerminal extends AcsExtendedCardTerminal {

    public Acr1255UCardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingEscapeCommand();
    }

    @Override
    public void startPolling() throws CardException {
        requireSetting("PICC", setPICCUsingEscapeCommand(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B, POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B));
        // do not enforce ISO14443-4, so that i.e. MIFARE Ultralight cards are also detected
        requireSetting("automatic polling", setAutomaticPICCPolling(AUTOMATIC_PICC_POLLING, AUTOMATIC_PICC_POLLING));
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0);
    }
}
