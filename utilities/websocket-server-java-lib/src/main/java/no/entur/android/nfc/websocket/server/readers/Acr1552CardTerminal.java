package no.entur.android.nfc.websocket.server.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.ByteArrayHexStringConverter;

/**
 * ACS ACR1552U. The PICC operating parameter is two bytes.
 */

public class Acr1552CardTerminal extends AcsExtendedCardTerminal {

    private final static Logger LOGGER = LoggerFactory.getLogger(Acr1552CardTerminal.class);

    // PICC polling type, first (high) byte; the second (low) byte holds PicoPass, ISO15693 and CTS
    public static final int PICC_ISO14443_TYPE_A = POLL_ISO14443_TYPE_A << 8;
    public static final int PICC_ISO14443_TYPE_B = POLL_ISO14443_TYPE_B << 8;

    public Acr1552CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingEscapeCommand();
    }

    // verified on an ACR1552U, FW 1.01.01
    @Override
    protected boolean isTransparentSessionSupported() {
        return true;
    }

    /**
     * After a transparent session, the card stays activated in a generic mode where i.e. READ BINARY is not supported,
     * so have the reader detect the card again by toggling automatic polling (which is on while polling for cards).
     */

    @Override
    protected void restoreAfterTransparentSession() throws CardException {
        redetectCardByTogglingAutomaticPolling(AUTOMATIC_PICC_POLLING);
    }

    /**
     * Set PICC polling type using escape command E0 00 01 20 02 [picc high] [picc low].
     * Note that ISO14443 type A and B are in the high byte, see {@link #PICC_ISO14443_TYPE_A}.
     *
     * @param picc polling type (two bytes)
     * @return true if the reader confirmed the new value
     */

    @Override
    public boolean setPICC(int picc) throws CardException {
        LOGGER.info("Set PICC " + Integer.toHexString(picc));

        if ((picc & 0xFFFF) != picc) {
            throw new RuntimeException("Unexpected PICC " + Integer.toHexString(picc));
        }

        byte high = (byte) ((picc >> 8) & 0xFF);
        byte low = (byte) (picc & 0xFF);

        byte[] data = transmitEscapeCommand(0x01, 0x20, new byte[]{high, low});

        if (data.length < 2 || data[0] != high || data[1] != low) {
            LOGGER.warn("Unable to properly update PICC: Expected " + Integer.toHexString(picc) + " got " + ByteArrayHexStringConverter.toHexString(data));

            return false;
        }
        return true;
    }

    @Override
    public void startPolling() throws CardException {
        setPICC(PICC_ISO14443_TYPE_A | PICC_ISO14443_TYPE_B);
        // do not enforce ISO14443-4, so that i.e. MIFARE Ultralight cards are also detected
        setAutomaticPICCPolling(AUTOMATIC_PICC_POLLING);
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0);
    }
}
