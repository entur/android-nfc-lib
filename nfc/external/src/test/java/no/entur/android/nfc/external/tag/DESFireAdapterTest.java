package no.entur.android.nfc.external.tag;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.Parcelable;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class DESFireAdapterTest {

    private static class FakeReader extends AbstractReaderIsoDepWrapper {

        private final Deque<byte[]> responses = new ArrayDeque<>();

        public FakeReader(String... responses) {
            super(0);
            for (String response : responses) {
                this.responses.add(hex(response));
            }
        }

        @Override
        public byte[] transceive(byte[] data) {
            return responses.removeFirst();
        }

        @Override
        public byte[] transceiveRaw(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Parcelable transceive(Parcelable parcelable) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testResponseToRaw() {
        // data + 91 XX -> XX + data
        assertArrayEquals(hex("00010203"), DESFireAdapter.responseAPDUToRaw(hex("0102039100")));
        assertArrayEquals(hex("00"), DESFireAdapter.responseAPDUToRaw(hex("9100")));
        assertArrayEquals(hex("AE"), DESFireAdapter.responseAPDUToRaw(hex("91AE")));
    }

    @Test
    public void testIsoErrorIsNotDesfireOk() {
        // 67 00 is wrong length, not DESFire status 00 (OK)
        assertArrayEquals(hex("6700"), DESFireAdapter.responseAPDUToRaw(hex("6700")));
        assertArrayEquals(hex("6A82"), DESFireAdapter.responseAPDUToRaw(hex("6A82")));
        assertArrayEquals(hex("00"), DESFireAdapter.responseAPDUToRaw(hex("00")));
        assertArrayEquals(new byte[0], DESFireAdapter.responseAPDUToRaw(new byte[0]));

        assertTrue(DESFireAdapter.isDesfireResponse(hex("9100")));
        assertFalse(DESFireAdapter.isDesfireResponse(hex("6700")));
        assertFalse(DESFireAdapter.isDesfireResponse(hex("00")));
        assertFalse(DESFireAdapter.isDesfireResponse(null));
    }

    @Test
    public void testChain() throws Exception {
        DESFireAdapter adapter = new DESFireAdapter(new FakeReader("AABB91AF", "CC9100"), false);

        assertArrayEquals(hex("00AABBCC"), adapter.transmitChain(hex("60")));
    }

    @Test
    public void testChainIsoError() throws Exception {
        DESFireAdapter adapter = new DESFireAdapter(new FakeReader("6700"), false);

        assertArrayEquals(hex("6700"), adapter.transmitChain(hex("60")));
    }

    @Test
    public void testChainIsoErrorInAdditionalFrame() {
        DESFireAdapter adapter = new DESFireAdapter(new FakeReader("AABB91AF", "6700"), false);

        try {
            adapter.transmitChain(hex("60"));
            fail();
        } catch (Exception e) {
            // expected
        }
    }

    private static byte[] hex(String string) {
        return ByteArrayHexStringConverter.hexStringToByteArray(string);
    }
}
