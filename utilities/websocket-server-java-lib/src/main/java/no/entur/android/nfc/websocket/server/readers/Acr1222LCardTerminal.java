package no.entur.android.nfc.websocket.server.readers;

import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1222L. Polling is started and stopped via the card types to poll for (PICC parameter);
 * the automatic PICC polling setting is left as is.
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

    // verified on an ACR1222L, FW ACR1222L-U V312.05: native DESFire SelectApplication returns 00 90 00
    @Override
    public boolean isStatusWordAppendedToSingleByteResponses() {
        return true;
    }

    // verified on an ACR1222L, FW ACR1222L-U V312.05 (no PC/SC transparent session, but a PN532 compatible chip)
    @Override
    public List<byte[]> transceiveTransparent(List<byte[]> commands) throws CardException {
        return transceiveInCommunicateThru(commands);
    }
}
