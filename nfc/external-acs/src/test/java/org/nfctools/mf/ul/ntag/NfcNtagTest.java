package org.nfctools.mf.ul.ntag;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.nfctools.mf.MfException;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class NfcNtagTest {

    private static class FixedResponseIsoDepWrapper extends ACSIsoDepWrapper {

        private final byte[] response;
        private byte[] request;

        public FixedResponseIsoDepWrapper(byte[] response) {
            super(null, 0);
            this.response = response;
        }

        @Override
        public byte[] transceive(byte[] data) {
            this.request = data;
            return response;
        }
    }

    @Test
    public void testReadCnt() throws Exception {
        // InCommunicateThru response D5 43 status, counter (LSB first), SW 90 00
        FixedResponseIsoDepWrapper wrapper = new FixedResponseIsoDepWrapper(ByteArrayHexStringConverter.hexStringToByteArray("D54300FF80819000"));

        NfcNtag ntag = new NfcNtag(wrapper);

        assertEquals(0x8180FF, ntag.readCnt());

        // FF 00 00 00 Lc D4 42 READ_CNT 02
        assertArrayEquals(ByteArrayHexStringConverter.hexStringToByteArray("FF00000004D4423902"), wrapper.request);
    }

    @Test
    public void testNoResponse() throws Exception {
        // status 01: timeout, i.e. an Ultralight and GET_VERSION
        FixedResponseIsoDepWrapper wrapper = new FixedResponseIsoDepWrapper(ByteArrayHexStringConverter.hexStringToByteArray("D543019000"));

        try {
            new NfcNtag(wrapper).getVersion();
            fail();
        } catch (MfException e) {
            // expected
        }
        // the tag is not selected again
        assertArrayEquals(ByteArrayHexStringConverter.hexStringToByteArray("FF00000003D44260"), wrapper.request);
    }

    @Test
    public void testUnexpectedResponse() throws Exception {
        for (String hex : new String[] { "9000", "6300", "AABBCC9000" }) {
            FixedResponseIsoDepWrapper wrapper = new FixedResponseIsoDepWrapper(ByteArrayHexStringConverter.hexStringToByteArray(hex));
            try {
                new NfcNtag(wrapper).getVersion();
                fail(hex);
            } catch (MfException e) {
                // expected
            }
        }
    }
}
