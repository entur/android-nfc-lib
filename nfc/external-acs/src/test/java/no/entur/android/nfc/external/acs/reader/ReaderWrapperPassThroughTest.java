package no.entur.android.nfc.external.acs.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import org.junit.Test;
import org.mockito.Mockito;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class ReaderWrapperPassThroughTest {

    private static ReaderWrapper newReaderWrapper() {
        // the ACS reader is not used, so skip the constructor
        return mock(ReaderWrapper.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    }

    @Test
    public void testTransmitPassThroughUsesInCommunicateThru() throws Exception {
        ReaderWrapper wrapper = newReaderWrapper();

        // D5 43 (InCommunicateThru response), status 00, then the tag response
        byte[] response = ByteArrayHexStringConverter.hexStringToByteArray("D543000004040201000F039000");
        doReturn(response).when(wrapper).transmit(eq(1), any(byte[].class));

        byte[] content = wrapper.transmitPassThrough(1, new byte[] { 0x60 });

        assertArrayEquals(ByteArrayHexStringConverter.hexStringToByteArray("0004040201000F03"), content);

        // FF 00 00 00 Lc D4 42 (InCommunicateThru) <command>
        verify(wrapper).transmit(1, ByteArrayHexStringConverter.hexStringToByteArray("FF00000003D44260"));
    }

    @Test
    public void testTransmitPassThroughStatusError() throws Exception {
        ReaderWrapper wrapper = newReaderWrapper();

        // status 01: timeout
        byte[] response = ByteArrayHexStringConverter.hexStringToByteArray("D543019000");
        doReturn(response).when(wrapper).transmit(eq(0), any(byte[].class));

        try {
            wrapper.transmitPassThrough(0, new byte[] { 0x60 });
            fail();
        } catch (PassthroughCommandException e) {
            assertEquals(1, e.getError());
        }
    }

    @Test
    public void testControlPassThroughUsesInCommunicateThru() throws Exception {
        ReaderWrapper wrapper = newReaderWrapper();

        byte[] response = ByteArrayHexStringConverter.hexStringToByteArray("D54300AABB9000");
        doReturn(response).when(wrapper).control(eq(0), eq(3500), any(byte[].class));

        byte[] content = wrapper.controlPassThrough(0, 3500, new byte[] { 0x3C, 0x00 });

        assertArrayEquals(new byte[] { (byte) 0xAA, (byte) 0xBB }, content);

        verify(wrapper).control(0, 3500, ByteArrayHexStringConverter.hexStringToByteArray("FF00000004D4423C00"));
    }
}
