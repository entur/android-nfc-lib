package no.entur.android.nfc.websocket.server.readers;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
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
        setPICC(AUTO_PICC_POLLING | AUTO_ATS_GENERATION | POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B);
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

    // PN532 InListPassiveTarget, one ISO 14443-A target at 106 kbps
    private static final byte[] IN_LIST_PASSIVE_TARGET = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x04, (byte) 0xD4, 0x4A, 0x01, 0x00};

    /**
     * Exchange native commands using the PN532 InCommunicateThru (D4 42), sent as a pseudo-APDU via escape commands.
     * A card which does not respond to a command (i.e. not supported) goes to the IDLE state, so then the card is
     * selected again using InListPassiveTarget (D4 4A). The card is not re-activated otherwise.
     * Verified on an ACR122U, FW ACR122U210.
     */

    @Override
    public List<byte[]> transceiveTransparent(List<byte[]> commands) throws CardException {
        Card ca = delegate.connect("DIRECT");
        try {
            ca.beginExclusive();
            try {
                List<byte[]> responses = new ArrayList<>(commands.size());
                for (byte[] command : commands) {
                    byte[] response = getInCommunicateThruResponse(ca.transmitControlCommand(SCARD_CTL_CODE, inCommunicateThru(command)));
                    if(response == null) {
                        // card did not respond, select it again
                        ca.transmitControlCommand(SCARD_CTL_CODE, IN_LIST_PASSIVE_TARGET);
                    }
                    responses.add(response);
                }
                return responses;
            } finally {
                ca.endExclusive();
            }
        } finally {
            disconnect(ca);
        }
    }

    private static byte[] inCommunicateThru(byte[] command) throws AcsCardException {
        if(command.length > 250) {
            throw new AcsCardException("Command too long for InCommunicateThru");
        }
        byte[] apdu = new byte[7 + command.length];
        apdu[0] = (byte) 0xFF;
        apdu[4] = (byte) (2 + command.length);
        apdu[5] = (byte) 0xD4;
        apdu[6] = 0x42;
        System.arraycopy(command, 0, apdu, 7, command.length);
        return apdu;
    }

    /**
     * @param response D5 43 [status] [card response] 90 00
     * @return card response, or null if the card did not respond (status not 00)
     */

    protected static byte[] getInCommunicateThruResponse(byte[] response) {
        if(response == null || response.length < 5) {
            return null;
        }
        if(response[0] != (byte) 0xD5 || response[1] != 0x43 || response[2] != 0x00 || (response[response.length - 2] & 0xFF) != 0x90 || response[response.length - 1] != 0x00) {
            return null;
        }
        byte[] data = new byte[response.length - 5];
        System.arraycopy(response, 3, data, 0, data.length);
        return data;
    }
}
