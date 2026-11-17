package no.entur.android.nfc.websocket.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Translates native MIFARE Ultralight commands (as sent by Android's {@code MifareUltralight} and {@code NfcA})
 * to PC/SC storage card commands (PC/SC 2.01 part 3), and the responses back.
 * <br>
 * Supported commands:
 * <ul>
 *     <li>READ (0x30): READ BINARY FF B0 00 [page] 10, returns 16 bytes (4 pages)</li>
 *     <li>WRITE (0xA2): UPDATE BINARY FF D6 00 [page] 04 [data], returns ACK (0x0A)</li>
 *     <li>FAST_READ (0x3A): emulated using READ BINARY, returns (end - start + 1) pages</li>
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
        int cardName = ((historicalBytes[9] & 0xFF) << 8) | (historicalBytes[10] & 0xFF);

        return cardName == CARD_NAME_MIFARE_ULTRALIGHT || cardName == CARD_NAME_MIFARE_ULTRALIGHT_C;
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
                int length = (end - start + 1) * PAGE_SIZE;

                ByteArrayOutputStream bout = new ByteArrayOutputStream(length + 4 * PAGE_SIZE);
                for (int page = start; page <= end; page += 4) {
                    byte[] data = read(page, transmitter);
                    bout.write(data, 0, data.length);
                }
                byte[] pages = bout.toByteArray();
                if(pages.length > length) {
                    byte[] result = new byte[length];
                    System.arraycopy(pages, 0, result, 0, length);
                    return result;
                }
                return pages;
            }
            default: {
                throw new IOException("Unsupported MIFARE Ultralight command " + ByteArrayHexStringConverter.toHexString(new byte[]{command[0]}));
            }
        }
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

    private static void checkLength(byte[] command, int length) throws IOException {
        if(command.length != length) {
            throw new IOException("Unexpected length " + command.length + " for command " + ByteArrayHexStringConverter.toHexString(new byte[]{command[0]}) + ", expected " + length);
        }
    }
}
