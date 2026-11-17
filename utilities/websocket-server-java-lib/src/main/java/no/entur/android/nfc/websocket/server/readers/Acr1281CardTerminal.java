package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1281U.
 */

public class Acr1281CardTerminal extends AcsExtendedCardTerminal {

    public Acr1281CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingEscapeCommand();
    }

    // Note: the PC/SC transparent session works on an ACR1281U (FW ACR1281U_V506.06), but is not enabled:
    // afterwards READ BINARY fails until the card is detected again, the card is not reported absent when
    // automatic polling is turned off (so each detection took about a second), and in a test with six taps of an
    // NTAG216, one tap failed to read the version and one tap failed the following card commands.

    @Override
    public void startPolling() throws CardException {
        requireSetting("PICC", setPICCUsingEscapeCommand(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B, POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B));
        // enforce ISO14443-4, otherwise a DESFire is only activated at layer 3 (no ISO-DEP).
        // Verified on an ACR1281U (FW ACR1281U_V506.06): MIFARE Ultralight and NTAG cards are still detected.
        requireSetting("automatic polling", setAutomaticPICCPolling(AUTOMATIC_PICC_POLLING | AUTOMATIC_PICC_POLLING_ENFORCE_ISO14443A_PART_4, AUTOMATIC_PICC_POLLING));
    }

    // verified on an ACR1281U, FW ACR1281U_V506.06: native DESFire SelectApplication returns A0 90 00
    @Override
    public boolean isStatusWordAppendedToSingleByteResponses() {
        return true;
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0);
    }
}
