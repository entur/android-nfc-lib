package no.entur.android.nfc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class ResponseAPDUTest {

    @Test
    public void testGetResponses() {
        byte[] payload = new byte[] { 1, 2, 3, 4, 5 };

        // 2 bytes per chunk
        List<ResponseAPDU> responses = ResponseAPDU.getResponses(payload, 9);

        assertEquals(3, responses.size());

        assertArrayEquals(new byte[] { 1, 2 }, responses.get(0).getData());
        assertEquals(0x90, responses.get(0).getSW1());
        assertEquals(0xAF, responses.get(0).getSW2());

        assertArrayEquals(new byte[] { 3, 4 }, responses.get(1).getData());
        assertEquals(0xAF, responses.get(1).getSW2());

        assertArrayEquals(new byte[] { 5 }, responses.get(2).getData());
        assertEquals(0x90, responses.get(2).getSW1());
        assertEquals(0x00, responses.get(2).getSW2());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 5000)
    public void testGetResponsesRejectsTooSmallMaxLength() {
        ResponseAPDU.getResponses(new byte[] { 1, 2, 3 }, 7);
    }
}
