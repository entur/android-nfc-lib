package no.entur.android.nfc.websocket.server.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        return setPICC(picc, 0xFF);
    }

    /**
     * Set PICC operating parameter using pseudo-APDU FF 00 51 [picc] 00.
     *
     * @param picc operating parameter
     * @param mask bits to compare in the response
     * @return true if the reader confirmed the new value
     */

    public boolean setPICC(int picc, int mask) throws CardException {
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

        if ((operation & mask) != (picc & mask)) {
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
        return setAutomaticPICCPolling(value, 0xFF);
    }

    /**
     * Set automatic PICC polling using escape command E0 00 00 23 01 [value].
     *
     * @param value automatic PICC polling setting
     * @param mask bits to compare in the response
     * @return true if the reader confirmed the new value
     */

    public boolean setAutomaticPICCPolling(int value, int mask) throws CardException {
        LOGGER.info("Set automatic polling " + Integer.toHexString(value));

        if ((value & 0xFF) != value) {
            throw new RuntimeException("Unexpected automatic polling value " + Integer.toHexString(value));
        }

        // i.e. E1 00 00 00 01 <value>
        byte[] data = transmitEscapeCommand(0x00, 0x23, new byte[]{(byte) value});

        return isExpectedValue("automatic polling", data, value, mask);
    }

    /**
     * Fail if the reader did not confirm a setting needed for polling, so that polling is not reported as started
     * while the reader does not detect cards.
     *
     * @param name setting name
     * @param confirmed whether the reader confirmed the setting
     * @throws AcsCardException if not confirmed
     */

    protected static void requireSetting(String name, boolean confirmed) throws AcsCardException {
        if(!confirmed) {
            throw new AcsCardException("Reader did not confirm the " + name + " setting");
        }
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

    /**
     * Whether the reader supports PC/SC part 3 transparent sessions via escape commands (verified per reader).
     */

    protected boolean isTransparentSessionSupported() {
        return false;
    }

    /**
     * Exchange native commands using a PC/SC part 3 transparent session, sent as escape commands:
     * start session, switch protocol to ISO 14443-A layer 3, exchange each command, end session.
     * Switching protocol re-activates the card.
     */

    @Override
    public List<byte[]> transceiveTransparent(List<byte[]> commands) throws CardException {
        if(!isTransparentSessionSupported()) {
            return null;
        }
        try {
            return transceiveTransparentSession(commands);
        } finally {
            try {
                restoreAfterTransparentSession();
            } catch (CardException e) {
                LOGGER.warn("Problem restoring card after transparent session", e);
            }
        }
    }

    /**
     * Restore normal card handling after a transparent session, i.e. have the reader detect the card again.
     */

    protected void restoreAfterTransparentSession() throws CardException {
    }

    private static final long CARD_ABSENT_TIMEOUT = 1000;

    /**
     * Have the reader detect the card again by toggling automatic polling, i.e. after a transparent session
     * which leaves the card in a state where card commands (i.e. READ BINARY) do not work.
     *
     * @param automaticPolling automatic polling value while polling for cards
     */

    protected void redetectCardByTogglingAutomaticPolling(int automaticPolling) throws CardException {
        setAutomaticPICCPolling(0);
        delegate.waitForCardAbsent(CARD_ABSENT_TIMEOUT);
        setAutomaticPICCPolling(automaticPolling);
    }

    private List<byte[]> transceiveTransparentSession(List<byte[]> commands) throws CardException {
        Card ca = delegate.connect("DIRECT");
        try {
            ca.beginExclusive();
            try {
                checkTransparentStatus(ca.transmitControlCommand(SCARD_CTL_CODE, TRANSPARENT_START_SESSION));
                try {
                    byte[] switchProtocol = ca.transmitControlCommand(SCARD_CTL_CODE, TRANSPARENT_SWITCH_PROTOCOL_ISO14443A_LAYER_3);
                    if(!isTransparentSuccess(switchProtocol)) {
                        // i.e. the ACR1281U reports no response for an NTAG216, but exchanges still work.
                        // If the card cannot be reached, the exchanges return no response.
                        LOGGER.warn("Unexpected switch protocol response " + ByteArrayHexStringConverter.toHexString(switchProtocol) + ", continue");
                    }

                    List<byte[]> responses = new ArrayList<>(commands.size());
                    for (byte[] command : commands) {
                        if(command.length > 127) {
                            throw new AcsCardException("Command too long for transparent exchange");
                        }
                        byte[] exchange = new byte[7 + command.length];
                        exchange[0] = (byte) 0xFF;
                        exchange[1] = (byte) 0xC2;
                        exchange[2] = 0x00;
                        exchange[3] = 0x01; // transparent exchange
                        exchange[4] = (byte) (2 + command.length);
                        exchange[5] = (byte) 0x95; // transceive data object
                        exchange[6] = (byte) command.length;
                        System.arraycopy(command, 0, exchange, 7, command.length);

                        responses.add(getTransparentResponse(ca.transmitControlCommand(SCARD_CTL_CODE, exchange)));
                    }
                    return responses;
                } finally {
                    try {
                        ca.transmitControlCommand(SCARD_CTL_CODE, TRANSPARENT_END_SESSION);
                    } catch (CardException e) {
                        LOGGER.warn("Problem ending transparent session", e);
                    }
                }
            } finally {
                ca.endExclusive();
            }
        } finally {
            disconnect(ca);
        }
    }

    protected static final byte[] TRANSPARENT_START_SESSION = new byte[]{(byte) 0xFF, (byte) 0xC2, 0x00, 0x00, 0x02, (byte) 0x81, 0x00};
    protected static final byte[] TRANSPARENT_END_SESSION = new byte[]{(byte) 0xFF, (byte) 0xC2, 0x00, 0x00, 0x02, (byte) 0x82, 0x00};
    protected static final byte[] TRANSPARENT_SWITCH_PROTOCOL_ISO14443A_LAYER_3 = new byte[]{(byte) 0xFF, (byte) 0xC2, 0x00, 0x02, 0x04, (byte) 0x8F, 0x02, 0x00, 0x03};

    private static void checkTransparentStatus(byte[] response) throws AcsCardException {
        if(!isTransparentSuccess(response)) {
            throw new AcsCardException("Transparent session failed: " + ByteArrayHexStringConverter.toHexString(response));
        }
    }

    // response is data objects, then SW1 SW2. The generic error status data object C0 03 00 90 00 indicates success.
    private static boolean isTransparentSuccess(byte[] response) {
        if(response == null || response.length < 2 || (response[response.length - 2] & 0xFF) != 0x90 || response[response.length - 1] != 0x00) {
            return false;
        }
        byte[] status = getDataObject(response, 0xC0);

        return status != null && status.length == 3 && status[0] == 0x00 && (status[1] & 0xFF) == 0x90 && status[2] == 0x00;
    }

    /**
     * @param response transparent exchange response
     * @return card response (data object 97), or null if the card did not respond
     */

    protected static byte[] getTransparentResponse(byte[] response) {
        if(!isTransparentSuccess(response)) {
            return null;
        }
        return getDataObject(response, 0x97);
    }

    // simple BER-TLV with single byte tags, excluding the trailing status word
    protected static byte[] getDataObject(byte[] response, int tag) {
        int offset = 0;
        int limit = response.length - 2;
        while(offset + 2 <= limit) {
            int t = response[offset] & 0xFF;
            int length = response[offset + 1] & 0xFF;
            int valueOffset = offset + 2;
            if(length == 0x81) {
                if(valueOffset >= limit) {
                    return null;
                }
                length = response[valueOffset] & 0xFF;
                valueOffset++;
            } else if(length == 0x82) {
                if(valueOffset + 1 >= limit) {
                    return null;
                }
                length = ((response[valueOffset] & 0xFF) << 8) | (response[valueOffset + 1] & 0xFF);
                valueOffset += 2;
            } else if(length > 0x7F) {
                return null;
            }
            if(valueOffset + length > limit) {
                return null;
            }
            if(t == tag) {
                byte[] value = new byte[length];
                System.arraycopy(response, valueOffset, value, 0, length);
                return value;
            }
            offset = valueOffset + length;
        }
        return null;
    }

    // PN532 InListPassiveTarget, one ISO 14443-A target at 106 kbps
    private static final byte[] IN_LIST_PASSIVE_TARGET = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00, 0x04, (byte) 0xD4, 0x4A, 0x01, 0x00};

    /**
     * Exchange native commands using the PN532 InCommunicateThru (D4 42), sent as a pseudo-APDU via escape commands,
     * for readers with a PN532 (compatible) chip.
     * A card which does not respond to a command (i.e. not supported) goes to the IDLE state, so then the card is
     * selected again using InListPassiveTarget (D4 4A). The card is not re-activated otherwise.
     */

    protected List<byte[]> transceiveInCommunicateThru(List<byte[]> commands) throws CardException {
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
