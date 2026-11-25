package no.entur.android.nfc.websocket.server;

import java.util.Arrays;

/**
 * Some readers (i.e. ACR1252U) append a status word 90 00 to card responses of a single byte.
 * A native DESFire response is a status byte, optionally followed by data, so for native commands
 * the appended status word must be removed: i.e. SelectApplication returns 00 or A0, not 00 90 00 or A0 90 00.
 * <br>
 * DESFire commands are either native (first byte is the command code), ISO 7816-4 (class 00) or
 * native commands wrapped in ISO 7816-4 (class 90). Only the DESFire (EV1) native command codes are treated as
 * native, so that other commands (i.e. GlobalPlatform, class 80 and 84) keep the status word from the card.
 * <br>
 * The appended status word cannot always be told apart from card data: the status OK (00) followed by two data bytes
 * 90 00 is the same as the status OK with an appended status word. Successful reads (ReadData and ReadRecords) always
 * return data, so these are kept as-is, but i.e. the last frame of a chained read (AdditionalFrame) is not.
 * <br>
 * Commands which are only in later DESFire versions (i.e. EV2 AuthenticateEV2First) are not treated as native, so
 * their single byte responses keep the appended status word.
 */

public class DesfireResponseAdapter {

    // DESFire EV1 (and later) with ISO-DEP, i.e. ATS historical byte 80
    private static final byte[] DESFIRE_ATR = new byte[]{0x3B, (byte) 0x81, (byte) 0x80, 0x01, (byte) 0x80, (byte) 0x80};

    private static final int OPERATION_OK = 0x00;

    // DESFire EV1 native command codes, as in nfcjlib (DESFireEV1.Command) and libfreefare
    private static final boolean[] NATIVE_COMMANDS = codes(
            // security level
            0x0A, // Authenticate
            0x1A, // AuthenticateISO
            0xAA, // AuthenticateAES
            0x54, // ChangeKeySettings
            0x5C, // SetConfiguration
            0xC4, // ChangeKey
            0x64, // GetKeyVersion
            // PICC level
            0xCA, // CreateApplication
            0xDA, // DeleteApplication
            0x6A, // GetApplicationIDs
            0x6E, // FreeMemory
            0x6D, // GetDFNames
            0x45, // GetKeySettings
            0x5A, // SelectApplication
            0xFC, // FormatPICC
            0x60, // GetVersion
            0x51, // GetCardUID
            // application level
            0x6F, // GetFileIDs
            0x61, // GetISOFileIDs
            0xF5, // GetFileSettings
            0x5F, // ChangeFileSettings
            0xCD, // CreateStdDataFile
            0xCB, // CreateBackupDataFile
            0xCC, // CreateValueFile
            0xC1, // CreateLinearRecordFile
            0xC0, // CreateCyclicRecordFile
            0xDF, // DeleteFile
            // file level
            0xBD, // ReadData
            0x8D, // ReadData per a libfreefare constant, but its ReadData sends BD, so not treated as a read
            0x3D, // WriteData
            0x6C, // GetValue
            0x0C, // Credit
            0xDC, // Debit
            0x1C, // LimitedCredit
            0x3B, // WriteRecord
            0xBB, // ReadRecords
            0xEB, // ClearRecordFile
            0xC7, // CommitTransaction
            0xA7, // AbortTransaction
            0xAF  // AdditionalFrame
    );

    // reads, which return data if successful
    private static final boolean[] READ_COMMANDS = codes(0xBD, 0xBB);

    // DESFire EV1 status codes, as in nfcjlib (DESFireEV1.Response) and libfreefare
    private static final boolean[] STATUS_CODES = codes(
            0x00, 0x0C, 0x0E, 0x1C, 0x1E, 0x40, 0x7E, 0x9D, 0x9E, 0xA0, 0xA1, 0xAE, 0xAF, 0xBE, 0xC1, 0xCA, 0xCD, 0xCE, 0xDE, 0xEE, 0xF0, 0xF1
    );

    private static boolean[] codes(int... codes) {
        boolean[] result = new boolean[256];
        for (int code : codes) {
            result[code] = true;
        }
        return result;
    }

    public static boolean isDesfire(byte[] atr) {
        return Arrays.equals(DESFIRE_ATR, atr);
    }

    public static boolean isNativeCommand(byte[] command) {
        return command != null && command.length > 0 && NATIVE_COMMANDS[command[0] & 0xFF];
    }

    /**
     * Remove a status word appended by the reader to a single byte native response.
     *
     * @param command command sent to the card
     * @param response response from the reader
     * @return the card response
     */

    public static byte[] stripAppendedStatusWord(byte[] command, byte[] response) {
        if(!isNativeCommand(command) || response == null || response.length != 3 || response[1] != (byte) 0x90 || response[2] != 0x00) {
            return response;
        }
        int status = response[0] & 0xFF;
        if(!STATUS_CODES[status]) {
            return response;
        }
        if(status == OPERATION_OK && READ_COMMANDS[command[0] & 0xFF]) {
            // successful read, with data 90 00
            return response;
        }
        return new byte[]{response[0]};
    }
}
