package no.entur.android.nfc.external.acs.reader.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ACRCommandsTest {

    @Test
    public void testIs() {
        byte[] in = new byte[] { 0x01, (byte) 0x90, 0x03 };

        assertTrue(ACRCommands.isZero(in, 0x01));
        assertTrue(ACRCommands.isFirst(in, 0x90));
        assertTrue(ACRCommands.isSecond(in, 0x03));

        assertFalse(ACRCommands.isZero(in, 0x90));
        assertFalse(ACRCommands.isSecond(in, 0x90));
    }
}
