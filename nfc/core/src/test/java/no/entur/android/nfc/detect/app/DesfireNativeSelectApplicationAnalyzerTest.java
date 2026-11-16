package no.entur.android.nfc.detect.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DesfireNativeSelectApplicationAnalyzerTest {

    @Test
    public void testSuccess() {
        // native status: operation ok
        assertTrue(DesfireNativeSelectApplicationAnalyzer.isSuccess(new byte[] { 0x00 }));
    }

    @Test
    public void testFailure() {
        // native status: application not found
        assertFalse(DesfireNativeSelectApplicationAnalyzer.isSuccess(new byte[] { (byte) 0xA0 }));
        // ISO 7816 status words ending in 00 from a card which does not speak native DESFire
        assertFalse(DesfireNativeSelectApplicationAnalyzer.isSuccess(new byte[] { 0x6D, 0x00 }));
        assertFalse(DesfireNativeSelectApplicationAnalyzer.isSuccess(new byte[] { 0x67, 0x00 }));
        assertFalse(DesfireNativeSelectApplicationAnalyzer.isSuccess(new byte[0]));
        assertFalse(DesfireNativeSelectApplicationAnalyzer.isSuccess(null));
    }
}
