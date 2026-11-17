package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.ATR;

public class MifareUltralightPcscAdapterTest {

    // ATR of a MIFARE Ultralight paper ticket on an ACR1552U
    private static final byte[] ULTRALIGHT_ATR = hex("3B8F8001804F0CA0000003060300030000000068");

    /**
     * Fake PC/SC storage card: READ BINARY / UPDATE BINARY on a 16 page MIFARE Ultralight.
     */

    private static class FakeStorageCard implements MifareUltralightPcscAdapter.ApduTransmitter {

        private final byte[] memory = new byte[16 * 4];
        private final List<String> apdus = new ArrayList<>();

        private byte[] status = null;

        FakeStorageCard() {
            for (int i = 0; i < memory.length; i++) {
                memory[i] = (byte) i;
            }
        }

        @Override
        public byte[] transmit(byte[] apdu) {
            apdus.add(toHex(apdu));
            if(status != null) {
                return status;
            }
            int page = apdu[3] & 0xFF;
            if(apdu[1] == (byte) 0xB0) {
                int length = apdu[4] & 0xFF;
                if(page * 4 + length > memory.length) {
                    return new byte[]{0x6B, 0x00};
                }
                byte[] response = new byte[length + 2];
                System.arraycopy(memory, page * 4, response, 0, length);
                response[length] = (byte) 0x90;
                return response;
            }
            if(apdu[1] == (byte) 0xD6) {
                System.arraycopy(apdu, 5, memory, page * 4, apdu[4] & 0xFF);
                return new byte[]{(byte) 0x90, 0x00};
            }
            return new byte[]{0x6D, 0x00};
        }
    }

    private FakeStorageCard card;
    private MifareUltralightPcscAdapter adapter;

    @BeforeEach
    public void before() {
        card = new FakeStorageCard();
        adapter = new MifareUltralightPcscAdapter();
    }

    @Test
    public void testRead() throws IOException {
        byte[] response = adapter.transceive(new byte[]{0x30, 0x04}, card);

        assertArrayEquals(Arrays.copyOfRange(card.memory, 16, 32), response);
        assertEquals(Arrays.asList("FFB0000410"), card.apdus);
    }

    @Test
    public void testWrite() throws IOException {
        byte[] response = adapter.transceive(new byte[]{(byte) 0xA2, 0x05, 0x01, 0x02, 0x03, 0x04}, card);

        assertArrayEquals(new byte[]{0x0A}, response);
        assertEquals(Arrays.asList("FFD600050401020304"), card.apdus);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, Arrays.copyOfRange(card.memory, 20, 24));
    }

    @Test
    public void testFastRead() throws IOException {
        // pages 0 to 5, i.e. 24 bytes, read in chunks of 4 pages
        byte[] response = adapter.transceive(new byte[]{0x3A, 0x00, 0x05}, card);

        assertArrayEquals(Arrays.copyOfRange(card.memory, 0, 24), response);
        assertEquals(Arrays.asList("FFB0000010", "FFB0000410"), card.apdus);

        card.apdus.clear();

        assertArrayEquals(card.memory, adapter.transceive(new byte[]{0x3A, 0x00, 0x0F}, card));
        assertEquals(4, card.apdus.size());
    }

    @Test
    public void testErrorStatus() {
        card.status = new byte[]{0x63, 0x00};

        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x30, 0x04}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{(byte) 0xA2, 0x05, 0x01, 0x02, 0x03, 0x04}, card));
    }

    @Test
    public void testReadOutsideMemory() {
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x30, 0x0E}, card));
    }

    @Test
    public void testUnsupportedOrMalformedCommands() {
        // GET_VERSION is not supported, nothing must be sent to the card
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x60}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x30}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{(byte) 0xA2, 0x05, 0x01, 0x02, 0x03}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x3A, 0x05, 0x04}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[0], card));

        assertTrue(card.apdus.isEmpty());
    }

    @Test
    public void testIsMifareUltralight() {
        assertTrue(MifareUltralightPcscAdapter.isMifareUltralight(new ATR(ULTRALIGHT_ATR).getHistoricalBytes()));

        // MIFARE Ultralight C
        assertTrue(MifareUltralightPcscAdapter.isMifareUltralight(hex("804F0CA00000030603003A00000000")));

        // MIFARE Classic 1K
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralight(hex("804F0CA000000306030001000000006A")));
        // DESFire
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralight(new ATR(hex("3B8180018080")).getHistoricalBytes()));
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralight(null));
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralight(new byte[0]));
    }

    @Test
    public void testGetVersionAndReadSignature() throws Exception {
        byte[] version = hex("0004040201001303");
        byte[] signature = new byte[32];
        Arrays.fill(signature, (byte) 0x42);

        assertTrue(adapter.readVersionAndSignature(transparentTerminal(Arrays.asList(version, signature))));
        assertTrue(adapter.hasMifareUltralightVersion());

        assertArrayEquals(version, adapter.transceive(new byte[]{0x60}, card));
        assertArrayEquals(signature, adapter.transceive(new byte[]{0x3C, 0x00}, card));
        // answered from cache, nothing sent to the card
        assertTrue(card.apdus.isEmpty());

        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x60, 0x00}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x3C}, card));
    }

    @Test
    public void testGetVersionNotSupportedByCard() throws Exception {
        // i.e. original MIFARE Ultralight
        assertTrue(adapter.readVersionAndSignature(transparentTerminal(Arrays.asList(null, null))));
        assertFalse(adapter.hasMifareUltralightVersion());

        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x60}, card));
        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x3C, 0x00}, card));
    }

    @Test
    public void testGetVersionNotSupportedByReader() throws Exception {
        assertFalse(adapter.readVersionAndSignature(transparentTerminal(null)));

        assertThrows(IOException.class, () -> adapter.transceive(new byte[]{0x60}, card));
    }

    @Test
    public void testHasMifareUltralightVersion() throws Exception {
        // MIFARE Ultralight EV1
        assertTrue(adapter.readVersionAndSignature(transparentTerminal(Arrays.asList(hex("0004030101000B03"), null))));
        assertTrue(adapter.hasMifareUltralightVersion());

        // other vendor
        assertTrue(adapter.readVersionAndSignature(transparentTerminal(Arrays.asList(hex("0005040201001303"), null))));
        assertFalse(adapter.hasMifareUltralightVersion());

        adapter.reset();
        assertFalse(adapter.hasMifareUltralightVersion());
    }

    @Test
    public void testIsMifareUltralightCandidate() {
        // MIFARE Ultralight paper ticket
        assertTrue(MifareUltralightPcscAdapter.isMifareUltralightCandidate(new ATR(ULTRALIGHT_ATR).getHistoricalBytes()));
        // NTAG216 on an ACR1552U, not identified by the reader
        assertTrue(MifareUltralightPcscAdapter.isMifareUltralightCandidate(new ATR(hex("3B868001804F03F000003B")).getHistoricalBytes()));

        // MIFARE Classic 1K
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralightCandidate(new ATR(hex("3B8F8001804F0CA000000306030001000000006A")).getHistoricalBytes()));
        // DESFire
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralightCandidate(new ATR(hex("3B8180018080")).getHistoricalBytes()));
        assertFalse(MifareUltralightPcscAdapter.isMifareUltralightCandidate(null));
    }

    private static ExtendedCardTerminal transparentTerminal(List<byte[]> responses) {
        return new FakeExtendedCardTerminal(responses);
    }

    private static class FakeExtendedCardTerminal extends ExtendedCardTerminal {

        private final List<byte[]> responses;

        FakeExtendedCardTerminal(List<byte[]> responses) {
            super(new FakeCardTerminal("Fake"));
            this.responses = responses;
        }

        @Override
        public List<byte[]> transceiveTransparent(List<byte[]> commands) {
            assertEquals(Arrays.asList("60", "3C00"), commands.stream().map(MifareUltralightPcscAdapterTest::toHex).collect(java.util.stream.Collectors.toList()));
            return responses;
        }

        @Override
        public void startPolling() {
        }

        @Override
        public void stopPolling() {
        }

        @Override
        public List<String> identifyTechnologies(javax.smartcardio.Card card, javax.smartcardio.CardChannel channel) {
            return java.util.Collections.emptyList();
        }

        @Override
        public String getName() {
            return "Fake";
        }

        @Override
        public javax.smartcardio.Card connect(String protocol) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCardPresent() {
            return false;
        }

        @Override
        public boolean waitForCardPresent(long timeout) {
            return false;
        }

        @Override
        public boolean waitForCardAbsent(long timeout) {
            return false;
        }
    }

    private static byte[] hex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }
}
