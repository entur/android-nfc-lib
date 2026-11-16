package no.entur.android.nfc.external.acs.tag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

/**
 * Same classification as Android's built-in NFC reader (NativeNfcTag.isUltralightC()).
 */

public class AcsMifareUltralightTypeTest {

    private static final byte[] UNLOCKED = hex("48000000");

    @Test
    public void testNdefFormatted() {
        // NTAG213 / Ultralight C: 144 bytes of NDEF data
        assertTrue(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("E1101200"), null));
        // NTAG215 / NTAG216
        assertTrue(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("E1103E00"), null));
        assertTrue(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("E1106D00"), null));
        // NTAG210 / Ultralight: 48 bytes of NDEF data
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("E1100600"), null));
        // locked
        assertTrue(AcsMifareUltralightTagServiceSupport.isUltralightC(hex("4800FFFF"), hex("E1101200"), null));
    }

    @Test
    public void testNdefMajorVersionTwo() {
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("E1201200"), null));
    }

    @Test
    public void testBlank() {
        // Ultralight C version info in page 4
        assertTrue(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("00000000"), hex("02000000")));
        // Ultralight
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("00000000"), hex("FFFFFFFF")));
        // NTAG 21x user memory
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("00000000"), hex("00000000")));
        // page 4 could not be read
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("00000000"), null));
    }

    @Test
    public void testOtherOtpContent() {
        assertFalse(AcsMifareUltralightTagServiceSupport.isUltralightC(UNLOCKED, hex("01020304"), hex("02000000")));
    }

    @Test
    public void testIsBlank() {
        assertTrue(AcsMifareUltralightTagServiceSupport.isBlank(UNLOCKED, hex("00000000")));
        assertFalse(AcsMifareUltralightTagServiceSupport.isBlank(hex("48000100"), hex("00000000")));
        assertFalse(AcsMifareUltralightTagServiceSupport.isBlank(UNLOCKED, hex("E1101200")));
    }

    private static byte[] hex(String string) {
        return ByteArrayHexStringConverter.hexStringToByteArray(string);
    }
}
