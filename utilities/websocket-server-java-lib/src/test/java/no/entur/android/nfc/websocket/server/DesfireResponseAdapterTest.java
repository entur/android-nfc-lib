package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import no.entur.android.nfc.websocket.server.readers.Acr1222LCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr122UCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1252CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1281CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1552CardTerminal;

public class DesfireResponseAdapterTest {

    private static final byte[] SELECT_APPLICATION = hex("5A008057");

    @Test
    public void testStripAppendedStatusWord() {
        // responses from an ACR1252U with a DESFire EV1
        assertArrayEquals(hex("00"), DesfireResponseAdapter.stripAppendedStatusWord(SELECT_APPLICATION, hex("009000")));
        // application not found
        assertArrayEquals(hex("A0"), DesfireResponseAdapter.stripAppendedStatusWord(SELECT_APPLICATION, hex("A09000")));
        // additional frame command
        assertArrayEquals(hex("AE"), DesfireResponseAdapter.stripAppendedStatusWord(hex("AF"), hex("AE9000")));
    }

    @Test
    public void testKeepResponses() {
        // already a single byte, i.e. ACR1552U
        assertArrayEquals(hex("00"), DesfireResponseAdapter.stripAppendedStatusWord(SELECT_APPLICATION, hex("00")));
        // multi-byte native response, not appended by the reader
        assertArrayEquals(hex("AF04010101001805"), DesfireResponseAdapter.stripAppendedStatusWord(hex("60"), hex("AF04010101001805")));
        // ISO 7816-4 command, status word from the card
        assertArrayEquals(hex("019000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("00B0000001"), hex("019000")));
        // wrapped native command
        assertArrayEquals(hex("019100"), DesfireResponseAdapter.stripAppendedStatusWord(hex("905A00000300805700"), hex("019100")));
        assertArrayEquals(hex("019000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("9060000000"), hex("019000")));
    }

    @Test
    public void testKeepResponsesToOtherCommands() {
        // proprietary class ISO 7816-4 commands (i.e. GlobalPlatform), one data byte and the status word from the card
        assertArrayEquals(hex("019000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("80CA00CF00"), hex("019000")));
        assertArrayEquals(hex("009000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("8482000010"), hex("009000")));
        // not a status code
        assertArrayEquals(hex("019000"), DesfireResponseAdapter.stripAppendedStatusWord(SELECT_APPLICATION, hex("019000")));
    }

    @Test
    public void testKeepReadData() {
        // a successful read has data, here 2 bytes 90 00 (read 2 bytes from offset 0 of file 1)
        assertArrayEquals(hex("009000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("BD01000000020000"), hex("009000")));
        assertArrayEquals(hex("009000"), DesfireResponseAdapter.stripAppendedStatusWord(hex("BB01000000010000"), hex("009000")));
        // but an error is a single byte
        assertArrayEquals(hex("F0"), DesfireResponseAdapter.stripAppendedStatusWord(hex("BD01000000020000"), hex("F09000")));
    }

    @Test
    public void testIsNativeCommand() {
        assertTrue(DesfireResponseAdapter.isNativeCommand(SELECT_APPLICATION));
        assertTrue(DesfireResponseAdapter.isNativeCommand(hex("AF")));
        assertFalse(DesfireResponseAdapter.isNativeCommand(hex("00A4040007")));
        assertFalse(DesfireResponseAdapter.isNativeCommand(hex("905A00000300805700")));
        assertFalse(DesfireResponseAdapter.isNativeCommand(hex("80CA00CF00")));
        assertFalse(DesfireResponseAdapter.isNativeCommand(hex("FFB0000410")));
        assertFalse(DesfireResponseAdapter.isNativeCommand(new byte[0]));
        assertFalse(DesfireResponseAdapter.isNativeCommand(null));
    }

    @Test
    public void testIsDesfire() {
        assertTrue(DesfireResponseAdapter.isDesfire(hex("3B8180018080")));
        assertFalse(DesfireResponseAdapter.isDesfire(hex("3B8F8001804F0CA0000003060300030000000068")));
        assertFalse(DesfireResponseAdapter.isDesfire(null));
    }

    @Test
    public void testReaders() {
        assertTrue(new Acr1252CardTerminal(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC")).isStatusWordAppendedToSingleByteResponses());
        assertTrue(new Acr122UCardTerminal(new FakeCardTerminal("ACS ACR122U 00 00")).isStatusWordAppendedToSingleByteResponses());
        assertTrue(new Acr1222LCardTerminal(new FakeCardTerminal("ACS ACR1222 3S PICC Reader 00 00")).isStatusWordAppendedToSingleByteResponses());
        assertTrue(new Acr1281CardTerminal(new FakeCardTerminal("ACS ACR1281 1S Dual Reader 00 01")).isStatusWordAppendedToSingleByteResponses());
        assertFalse(new Acr1552CardTerminal(new FakeCardTerminal("ACS ACR1552 1S CL Reader PICC")).isStatusWordAppendedToSingleByteResponses());
    }

    private static byte[] hex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
