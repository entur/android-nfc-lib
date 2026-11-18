package no.entur.android.nfc.wrapper.test.tech.transceive.ultralight;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;

public class MifareUltralightMockTransceiveTest {

    private static MifareUltralightMockTransceive ntag203() {
        // 42 pages, page n is n n n n
        byte[] memory = new byte[42 * 4];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = (byte) (i / 4);
        }
        return new MifareUltralightMockTransceive(memory);
    }

    @Test
    public void testReadRollsOverAtEndOfMemory() throws Exception {
        // like a real tag, pages 40, 41, 0, 1
        byte[] response = ntag203().transceive(new byte[] { 0x30, 40 }, false);

        assertArrayEquals(new byte[] { 40, 40, 40, 40, 41, 41, 41, 41, 0, 0, 0, 0, 1, 1, 1, 1 }, response);
    }

    @Test
    public void testRead() throws Exception {
        byte[] response = ntag203().transceive(new byte[] { 0x30, 4 }, false);

        assertEquals(16, response.length);
        assertEquals(4, response[0]);
        assertEquals(7, response[15]);
    }

    @Test
    public void testReadOutsideMemory() {
        try {
            ntag203().transceive(new byte[] { 0x30, 42 }, false);
            fail();
        } catch (IOException e) {
            // expected
        }
    }
}
