package no.entur.android.nfc.websocket.server.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;

import no.entur.android.nfc.websocket.server.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.server.ExtendedCardTerminal;
import no.entur.android.nfc.websocket.server.MifareUltralightPcscAdapter;

/**
 * Base class for ACS readers. Reader commands are sent as control commands over a direct connection, either as
 * pseudo-APDUs (FF 00 ..) or as ACS escape commands (E0 00 ..).
 */

public abstract class AcsExtendedCardTerminal extends ExtendedCardTerminal {

    private final static Logger LOGGER = LoggerFactory.getLogger(AcsExtendedCardTerminal.class);

    protected static final int SCARD_CTL_CODE = 0x310000 + 3500 * 4;

    // PICC operating parameter: card types to poll for (common to all ACS readers)
    public static final int POLL_ISO14443_TYPE_A = 1;
    public static final int POLL_ISO14443_TYPE_B = 1 << 1;

    // automatic PICC polling (escape command 0x23)
    public static final int AUTOMATIC_PICC_POLLING = 1;
    public static final int AUTOMATIC_PICC_POLLING_ENFORCE_ISO14443A_PART_4 = 1 << 7;

    public AcsExtendedCardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    public abstract String getFirmware() throws CardException;

    /**
     * Send a control command to the reader, using a direct connection (no card needed).
     *
     * @param command command
     * @return response
     * @throws CardException if the command could not be sent
     */

    protected byte[] transmitControlCommand(byte[] command) throws CardException {
        Card ca = delegate.connect("DIRECT");
        try {
            LOGGER.debug("Get exclusive lock");
            ca.beginExclusive();
            LOGGER.debug("Got exclusive lock");
            try {
                return ca.transmitControlCommand(SCARD_CTL_CODE, command);
            } finally {
                ca.endExclusive();
                LOGGER.debug("Released exclusive lock");
            }
        } finally {
            disconnect(ca);
        }
    }

    /**
     * Send an ACS escape command (E0 00 P1 INS [Lc data]) to the reader.
     *
     * @param p1 parameter 1
     * @param ins instruction
     * @param data command data, possibly empty
     * @return response data (from E1 00 00 00 Lc data)
     * @throws CardException if the command failed
     */

    protected byte[] transmitEscapeCommand(int p1, int ins, byte[] data) throws CardException {
        byte[] command;
        if(data.length == 0) {
            command = new byte[]{(byte) 0xE0, 0x00, (byte) p1, (byte) ins, 0x00};
        } else {
            command = new CommandAPDU(0xE0, 0x00, p1, ins, data).getBytes();
        }

        byte[] response = transmitControlCommand(command);

        CommandAPDU responseAPDU;
        try {
            responseAPDU = new CommandAPDU(response);
        } catch(IllegalArgumentException e) {
            throw new AcsCardException("Unexpected response " + ByteArrayHexStringConverter.toHexString(response), e);
        }
        if (!isSuccess(responseAPDU)) {
            throw new AcsCardException("Card responded with error " + ByteArrayHexStringConverter.toHexString(response));
        }
        return responseAPDU.getData();
    }

    /**
     * Read firmware using pseudo-APDU FF 00 48 00 00.
     */

    protected String getFirmwareUsingPseudoApdu() throws CardException {
        LOGGER.info("Read firmware for " + delegate.getName());

        // https://stackoverflow.com/questions/12265807/javax-smartcardio-transmit-to-nfc-usb-reader-without-card/12346874#12346874
        byte[] bytes = transmitControlCommand(new byte[]{(byte) 0xFF, 0x00, 0x48, 0x00, 0x00});

        String firmware = new String(bytes, StandardCharsets.US_ASCII);

        LOGGER.info("Read firmware " + firmware);

        return firmware;
    }

    /**
     * Read firmware using escape command E0 00 00 18 00.
     */

    protected String getFirmwareUsingEscapeCommand() throws CardException {
        LOGGER.info("Read firmware for " + delegate.getName());

        byte[] data = transmitEscapeCommand(0x00, 0x18, new byte[0]);

        String firmware = new String(data, StandardCharsets.US_ASCII);

        LOGGER.info("Read firmware " + firmware);

        return firmware;
    }

    /**
     * Set PICC operating parameter using pseudo-APDU FF 00 51 [picc] 00.
     *
     * @param picc operating parameter
     * @return true if the reader confirmed the new value
     */

    public boolean setPICC(int picc) throws CardException {
        LOGGER.info("Set PICC " + Integer.toHexString(picc));

        if ((picc & 0xFF) != picc) {
            throw new RuntimeException("Unexpected PICC " + Integer.toHexString(picc));
        }

        byte[] bytes = transmitControlCommand(new byte[] { (byte) 0xFF, 0x00, 0x51, (byte) picc, 0x00 });

        // i.e. 90 [picc]
        if (bytes.length < 2 || !isSuccessControl(bytes)) {
            LOGGER.debug("Unable to set PICC: " + Integer.toHexString(picc));

            throw new AcsCardException("Card responded with error code");
        }

        final int operation = bytes[1] & 0xFF;

        if (operation != picc) {
            LOGGER.warn("Unable to properly update PICC: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

            return false;
        }
        LOGGER.debug("Updated PICC " + Integer.toHexString(operation));

        return true;
    }

    /**
     * Set PICC operating parameter using escape command E0 00 00 20 01 [picc].
     *
     * @param picc operating parameter
     * @param mask bits to compare in the response
     * @return true if the reader confirmed the new value
     */

    protected boolean setPICCUsingEscapeCommand(int picc, int mask) throws CardException {
        LOGGER.info("Set PICC " + Integer.toHexString(picc));

        if ((picc & 0xFF) != picc) {
            throw new RuntimeException("Unexpected PICC " + Integer.toHexString(picc));
        }

        byte[] data = transmitEscapeCommand(0x00, 0x20, new byte[]{(byte) picc});

        return isExpectedValue("PICC", data, picc, mask);
    }

    /**
     * Set automatic PICC polling using escape command E0 00 00 23 01 [value].
     *
     * @param value automatic PICC polling setting
     * @return true if the reader confirmed the new value
     */

    public boolean setAutomaticPICCPolling(int value) throws CardException {
        LOGGER.info("Set automatic polling " + Integer.toHexString(value));

        if ((value & 0xFF) != value) {
            throw new RuntimeException("Unexpected automatic polling value " + Integer.toHexString(value));
        }

        // i.e. E1 00 00 00 01 <value>
        byte[] data = transmitEscapeCommand(0x00, 0x23, new byte[]{(byte) value});

        return isExpectedValue("automatic polling", data, value, 0xFF);
    }

    private static boolean isExpectedValue(String name, byte[] data, int expected, int mask) {
        if (data.length == 0) {
            LOGGER.warn("Unable to properly update " + name + ": Empty response");

            return false;
        }
        int value = data[0] & mask;
        if (value != (expected & mask)) {
            LOGGER.warn("Unable to properly update " + name + ": Expected " + Integer.toHexString(expected) + " got " + Integer.toHexString(value));

            return false;
        }
        LOGGER.debug("Updated " + name + " " + Integer.toHexString(value));

        return true;
    }

    protected static void disconnect(Card card) {
        try {
            card.disconnect(false);
        } catch (Exception e) {
            LOGGER.debug("Problem disconnecting DIRECT connection", e);
        }
    }

    public static boolean isSuccessControl(byte[] in) {
        return (in[in.length - 2] & 0xFF) == 0x90;
    }

    public static boolean isSuccess(CommandAPDU response) {
        return response.getCLA() == 0xE1 && response.getP1() == 0x00 && response.getP2() == 0x00 && response.getINS() == 0x00;
    }

    @Override
    public List<String> identifyTechnologies(Card card, CardChannel channel) {
        ATR atr = card.getATR();

        byte[] atrBytes = atr.getBytes();

        LOGGER.info("Got ATR " + ByteArrayHexStringConverter.toHexString(atrBytes));

        if(atrBytes.length == 6) {

            if(atrBytes[0] == (byte)0x3B &&
                    atrBytes[1] == (byte)0x81 &&
                    atrBytes[2] == (byte)0x80 &&
                    atrBytes[3] == (byte)0x01 &&
                    atrBytes[4] == (byte)0x80 &&
                    atrBytes[5] == (byte)0x80
            ) {
                return Arrays.asList("IsoDep");
            }
        }

        if(MifareUltralightPcscAdapter.isMifareUltralight(atr.getHistoricalBytes())) {
            return Arrays.asList("MifareUltralight");
        }

        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Card connect(String s) throws CardException {
        return delegate.connect(s);
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return delegate.isCardPresent();
    }

    @Override
    public boolean waitForCardPresent(long l) throws CardException {
        return delegate.waitForCardPresent(l);
    }

    @Override
    public boolean waitForCardAbsent(long l) throws CardException {
        return delegate.waitForCardAbsent(l);
    }
}
