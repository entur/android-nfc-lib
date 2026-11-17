package no.entur.android.nfc.websocket.server.readers;

import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR122U. Automatic polling is part of the PICC operating parameter.
 */

public class Acr122UCardTerminal extends AcsExtendedCardTerminal {

    public static final int AUTO_PICC_POLLING = 1 << 7;
    public static final int AUTO_ATS_GENERATION = 1 << 6;

    public Acr122UCardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingPseudoApdu();
    }

    @Override
    public void startPolling() throws CardException {
        requireSetting("PICC", setPICC(AUTO_PICC_POLLING | AUTO_ATS_GENERATION | POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B, AUTO_PICC_POLLING | POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B));
    }

    @Override
    public void stopPolling() throws CardException {
        setPICC(AUTO_ATS_GENERATION | POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B);
    }

    // verified on an ACR122U, FW ACR122U210: native DESFire SelectApplication returns 00 90 00
    @Override
    public boolean isStatusWordAppendedToSingleByteResponses() {
        return true;
    }

    // verified on an ACR122U, FW ACR122U210
    @Override
    public List<byte[]> transceiveTransparent(List<byte[]> commands) throws CardException {
        return transceiveInCommunicateThru(commands);
    }
}
