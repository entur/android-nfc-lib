package no.entur.android.nfc.wrapper.test.tech.transceive.ultralight;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class MifareUltralightMemoryBuilderTest {

    // UID 04 E1 41 2A 3B 5C 80
    private static final byte[] UID = hex("04E1412A3B5C80");

    // page 0: UID0 UID1 UID2 BCC0 (0x88 ^ 04 ^ E1 ^ 41 = 2C)
    // page 1: UID3 UID4 UID5 UID6
    // page 2: BCC1 (2A ^ 3B ^ 5C ^ 80 = CD), internal, lock bytes
    private static final byte[] HEADER = hex("04E1412C2A3B5C80CD480000");

    @Test
    public void testTagIdInMemory() throws Exception {
        MifareUltralightMockTransceive transceive = (MifareUltralightMockTransceive) new MifareUltralightMemoryBuilder()
                .withPage(2, new byte[] { 0x00, 0x48, 0x00, 0x00 })
                .withTagId(UID)
                .build();

        assertArrayEquals(HEADER, readPages(transceive));
    }

    @Test
    public void testSetTagId() throws Exception {
        byte[] memory = new byte[64];
        memory[9] = 0x48;

        MifareUltralightMockTransceive transceive = new MifareUltralightMockTransceive(memory);
        transceive.setTagId(UID);

        assertArrayEquals(HEADER, readPages(transceive));
    }

    private static byte[] hex(String string) {
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(string.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static byte[] readPages(MifareUltralightMockTransceive transceive) throws Exception {
        // READ returns 4 pages (16 bytes) starting at page 0
        byte[] response = transceive.transceive(new byte[] { 0x30, 0x00 }, false);

        byte[] header = new byte[12];
        System.arraycopy(response, 0, header, 0, header.length);
        return header;
    }
}
