package no.entur.android.nfc.websocket.server;

import java.util.Arrays;

/**
 * Some readers (i.e. ACR1252U) append a status word 90 00 to card responses of a single byte.
 * A native DESFire response is a status byte, optionally followed by data, so for native commands
 * the appended status word must be removed: i.e. SelectApplication returns 00 or A0, not 00 90 00 or A0 90 00.
 * <br>
 * DESFire commands are either native (first byte is the command code), ISO 7816-4 (class 00) or
 * native commands wrapped in ISO 7816-4 (class 90). No native command code is 00 or 90.
 */

public class DesfireResponseAdapter {

    // DESFire EV1 (and later) with ISO-DEP, i.e. ATS historical byte 80
    private static final byte[] DESFIRE_ATR = new byte[]{0x3B, (byte) 0x81, (byte) 0x80, 0x01, (byte) 0x80, (byte) 0x80};

    public static boolean isDesfire(byte[] atr) {
        return Arrays.equals(DESFIRE_ATR, atr);
    }

    public static boolean isNativeCommand(byte[] command) {
        return command != null && command.length > 0 && command[0] != 0x00 && command[0] != (byte) 0x90;
    }

    /**
     * Remove a status word appended by the reader to a single byte native response.
     *
     * @param command command sent to the card
     * @param response response from the reader
     * @return the card response
     */

    public static byte[] stripAppendedStatusWord(byte[] command, byte[] response) {
        if(isNativeCommand(command) && response != null && response.length == 3 && response[1] == (byte) 0x90 && response[2] == 0x00) {
            return new byte[]{response[0]};
        }
        return response;
    }
}
