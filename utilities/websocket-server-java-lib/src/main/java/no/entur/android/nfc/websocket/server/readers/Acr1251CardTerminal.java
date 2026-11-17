package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1251U, per the API manual 1.14. Uses the native PICC operating parameter (E0 00 00 20) and automatic PICC
 * polling (E0 00 00 23) escape commands, like the other ACS readers. Note that the ACR122U compatible PICC operating
 * parameter (FF 00 51) has another bit layout, where bit 7 is automatic PICC polling.
 */

public class Acr1251CardTerminal extends AcsExtendedCardTerminal {

    public Acr1251CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingPseudoApdu();
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
