package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1251U. There is no automatic polling setting, so polling is stopped by not polling for any card types.
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
        setPICC(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B);
    }

    @Override
    public void stopPolling() throws CardException {
        setPICC(0);
    }
}
