package no.entur.android.nfc.websocket.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CardException;

/**
 * Translates native MIFARE Ultralight commands (as sent by Android's {@code MifareUltralight} and {@code NfcA})
 * to PC/SC storage card commands (PC/SC 2.01 part 3), and the responses back.
 * <br>
 * Supported commands:
 * <ul>
 *     <li>READ (0x30): READ BINARY FF B0 00 [page] 10, returns 16 bytes (4 pages)</li>
 *     <li>WRITE (0xA2): UPDATE BINARY FF D6 00 [page] 04 [data], returns ACK (0x0A)</li>
 *     <li>FAST_READ (0x3A): emulated using READ BINARY, returns (end - start + 1) pages; does not read beyond the end page</li>
 *     <li>GET_VERSION (0x60) and READ_SIG (0x3C): returned from the values read when the card was detected,
 *     see {@link #readVersionAndSignature(ExtendedCardTerminal)}; fail if not supported by the card or the reader</li>
 * </ul>
 */

public class MifareUltralightPcscAdapter {

    public interface ApduTransmitter {
        /**
         * @param apdu command APDU
         * @return response APDU, including status word
         */
        byte[] transmit(byte[] apdu) throws IOException;
    }

    public static final int PAGE_SIZE = 4;

    public static final int READ = 0x30;
    public static final int WRITE = 0xA2;
    public static final int FAST_READ = 0x3A;
    public static final byte GET_VERSION = 0x60;
    public static final byte READ_SIG = 0x3C;

    private static final byte VENDOR_NXP = 0x04;
    private static final byte PRODUCT_TYPE_MIFARE_ULTRALIGHT = 0x03;
    private static final byte PRODUCT_TYPE_NTAG = 0x04;

    public static final byte ACK = 0x0A;

    // PC/SC part 3 card names
    private static final int CARD_NAME_MIFARE_ULTRALIGHT = 0x0003;
    private static final int CARD_NAME_MIFARE_ULTRALIGHT_C = 0x003A;

    private static final byte[] PCSC_RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x06};

    /**
     * Check whether historical bytes from a PC/SC (part 3) storage card ATR identifies a MIFARE Ultralight (or Ultralight C).
     * The historical bytes are like 80 4F 0C A0 00 00 03 06 [standard] [card name, 2 bytes] 00 00 00 00.
     *
     * @param historicalBytes historical bytes from the ATR
     * @return true if MIFARE Ultralight
     */

    public static boolean isMifareUltralight(byte[] historicalBytes) {
        if(!isPcscStorageCard(historicalBytes)) {
            return false;
        }
        int cardName = ((historicalBytes[9] & 0xFF) << 8) | (historicalBytes[10] & 0xFF);

        return cardName == CARD_NAME_MIFARE_ULTRALIGHT || cardName == CARD_NAME_MIFARE_ULTRALIGHT_C;
    }

    private static boolean isPcscStorageCard(byte[] historicalBytes) {
        if(historicalBytes == null || historicalBytes.length < 11) {
            return false;
        }
        if(historicalBytes[0] != (byte) 0x80 || historicalBytes[1] != 0x4F) {
            return false;
        }
        for (int i = 0; i < PCSC_RID.length; i++) {
            if(historicalBytes[3 + i] != PCSC_RID[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether a card might be a MIFARE Ultralight or NTAG, so that its version is worth reading:
     * either identified as MIFARE Ultralight, or a card without ISO-DEP which the reader did not identify
     * (i.e. an NTAG216 on an ACR1552U has historical bytes 80 4F 03 F0 00 00).
     *
     * @param historicalBytes historical bytes from the ATR
     * @return true if candidate
     */

    public static boolean isMifareUltralightCandidate(byte[] historicalBytes) {
        if(isMifareUltralight(historicalBytes)) {
            return true;
        }
        return historicalBytes != null && historicalBytes.length >= 2 && historicalBytes[0] == (byte) 0x80 && historicalBytes[1] == 0x4F && !isPcscStorageCard(historicalBytes);
    }

    // cached GET_VERSION and READ_SIG responses, null if not available
    private volatile byte[] version;
    private volatile byte[] signature;

    /**
     * Read version (GET_VERSION) and signature (READ_SIG) via a transparent exchange, if supported by the terminal.
     * These are static for a card, so they are cached and returned for later commands. Reading them re-activates
     * the card, so this is done before the card is used.
     *
     * @param terminal terminal
     * @return true if the transparent exchange was performed, so any connection to the card must be reestablished
     */

    public boolean readVersionAndSignature(ExtendedCardTerminal terminal) throws CardException {
        reset();

        List<byte[]> responses = terminal.transceiveTransparent(Arrays.asList(new byte[]{GET_VERSION}, new byte[]{READ_SIG, 0x00}));
        if(responses == null) {
            return false;
        }
        this.version = responses.get(0);
        this.signature = responses.get(1);
        return true;
    }

    /**
     * @return true if the version is from a NXP MIFARE Ultralight (EV1 and later) or NTAG
     */

    public boolean hasMifareUltralightVersion() {
        byte[] version = this.version;
        return version != null && version.length >= 3 && version[1] == VENDOR_NXP && (version[2] == PRODUCT_TYPE_MIFARE_ULTRALIGHT || version[2] == PRODUCT_TYPE_NTAG);
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void reset() {
        this.version = null;
        this.signature = null;
    }

    public byte[] transceive(byte[] command, ApduTransmitter transmitter) throws IOException {
        if(command == null || command.length == 0) {
            throw new IOException("Empty command");
        }
        int code = command[0] & 0xFF;
        switch (code) {
            case READ: {
                checkLength(command, 2);

                return read(command[1] & 0xFF, transmitter);
            }
            case WRITE: {
                checkLength(command, 2 + PAGE_SIZE);

                byte[] apdu = new byte[]{(byte) 0xFF, (byte) 0xD6, 0x00, command[1], PAGE_SIZE, command[2], command[3], command[4], command[5]};

                getData(apdu, transmitter.transmit(apdu));

                return new byte[]{ACK};
            }
            case FAST_READ: {
                checkLength(command, 3);

                int start = command[1] & 0xFF;
                int end = command[2] & 0xFF;
                if(end < start) {
                    throw new IOException("Unexpected FAST_READ range " + start + " to " + end);
                }
                return fastRead(start, end, transmitter);
            }
            case GET_VERSION: {
                checkLength(command, 1);

                return getCached("GET_VERSION", version);
            }
            case READ_SIG: {
                checkLength(command, 2);

                return getCached("READ_SIG", signature);
            }
            default: {
                throw new IOException("Unsupported MIFARE Ultralight command " + ByteArrayHexStringConverter.toHexString(new byte[]{command[0]}));
            }
        }
    }

    /**
     * Read pages using READ BINARY of 4 pages each, without reading beyond the last page (which might be the end of
     * the memory, or followed by protected pages): the last read ends at the last page, reading some pages twice.
     */

    private byte[] fastRead(int start, int end, ApduTransmitter transmitter) throws IOException {
        byte[] result = new byte[(end - start + 1) * PAGE_SIZE];

        // pages before the first page can be read if the first page can
        int lastReadPage = Math.max(0, end - 3);

        int page = start;
        while(page <= end) {
            int readPage = Math.min(page, lastReadPage);

            byte[] data = read(readPage, transmitter);

            int offset = (page - readPage) * PAGE_SIZE;
            int count = Math.min(data.length - offset, (end - page + 1) * PAGE_SIZE);
            System.arraycopy(data, offset, result, (page - start) * PAGE_SIZE, count);

            page += count / PAGE_SIZE;
        }
        return result;
    }

    private byte[] read(int page, ApduTransmitter transmitter) throws IOException {
        byte[] apdu = new byte[]{(byte) 0xFF, (byte) 0xB0, 0x00, (byte) page, 4 * PAGE_SIZE};

        byte[] data = getData(apdu, transmitter.transmit(apdu));
        if(data.length != 4 * PAGE_SIZE) {
            throw new IOException("Unexpected READ BINARY response length " + data.length + " for page " + page);
        }
        return data;
    }

    private static byte[] getData(byte[] apdu, byte[] response) throws IOException {
        if(response == null || response.length < 2) {
            throw new IOException("Unexpected response to " + ByteArrayHexStringConverter.toHexString(apdu));
        }
        int sw1 = response[response.length - 2] & 0xFF;
        int sw2 = response[response.length - 1] & 0xFF;
        if(sw1 != 0x90 || sw2 != 0x00) {
            throw new IOException("Card responded with " + ByteArrayHexStringConverter.toHexString(new byte[]{(byte) sw1, (byte) sw2}) + " to " + ByteArrayHexStringConverter.toHexString(apdu));
        }
        byte[] data = new byte[response.length - 2];
        System.arraycopy(response, 0, data, 0, data.length);
        return data;
    }

    private static byte[] getCached(String name, byte[] value) throws IOException {
        if(value == null) {
            // not supported by the card (i.e. original MIFARE Ultralight) or the reader
            throw new IOException(name + " not available");
        }
        return value.clone();
    }

    private static void checkLength(byte[] command, int length) throws IOException {
        if(command.length != length) {
            throw new IOException("Unexpected length " + command.length + " for command " + ByteArrayHexStringConverter.toHexString(new byte[]{command[0]}) + ", expected " + length);
        }
    }
}
