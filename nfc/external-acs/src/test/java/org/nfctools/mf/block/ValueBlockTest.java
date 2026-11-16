package org.nfctools.mf.block;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class ValueBlockTest {

    @Test
    public void testEncodeLittleEndian() {
        ValueBlock block = new ValueBlock(0x12345678, (byte) 0x05);

        // value LSB first, inverted value, value, address, ~address, address, ~address
        byte[] expected = ByteArrayHexStringConverter.hexStringToByteArray("78563412" + "87A9CBED" + "78563412" + "05FA05FA");

        assertArrayEquals(expected, block.getData());
    }

    @Test
    public void testDecodeLittleEndian() throws Exception {
        byte[] data = ByteArrayHexStringConverter.hexStringToByteArray("01000000" + "FEFFFFFF" + "01000000" + "05FA05FA");

        ValueBlock block = new ValueBlock(data);

        assertEquals(1, block.getValue());
        assertEquals((byte) 0x05, block.getAddress());
    }

    @Test
    public void testRoundTrip() throws Exception {
        for (int value : new int[] { 0, 1, -1, 255, 256, Integer.MAX_VALUE, Integer.MIN_VALUE, -12345 }) {
            ValueBlock block = new ValueBlock(new ValueBlock(value, (byte) 0x7F).getData());

            assertEquals(value, block.getValue());
            assertEquals((byte) 0x7F, block.getAddress());
        }
    }
}
