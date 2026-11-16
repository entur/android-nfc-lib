package no.entur.android.nfc.external.acs.reader.command;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.smartcard.ReaderException;

import org.junit.Test;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;

public class ACSIsoDepWrapperTest {

    private static final byte[] GET_VERSION = new byte[] { 0x60 };
    private static final byte[] VERSION = new byte[] { 0x00, 0x04, 0x04, 0x02, 0x01, 0x00, 0x0F, 0x03 };

    @Test
    public void testTransceiveRawUsesPassThrough() throws Exception {
        ReaderWrapper reader = mock(ReaderWrapper.class);
        when(reader.getReaderName()).thenReturn("ACS ACR122U PICC Interface");
        when(reader.transmitPassThrough(1, GET_VERSION)).thenReturn(VERSION);

        ACSIsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, 1);

        assertArrayEquals(VERSION, wrapper.transceiveRaw(GET_VERSION));
    }

    @Test
    public void testTransceiveRawNotSupported() throws Exception {
        ReaderWrapper reader = mock(ReaderWrapper.class);
        when(reader.getReaderName()).thenReturn("ACS ACR1252 1S CL Reader PICC");

        ACSIsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, 0);

        try {
            wrapper.transceiveRaw(GET_VERSION);
            fail();
        } catch (ReaderException e) {
            // expected
        }
        verify(reader, never()).transmitPassThrough(anyInt(), any(byte[].class));
    }

    @Test
    public void testPassThroughReaders() {
        assertTrue(ReaderWrapper.supportsPassThrough("ACS ACR122U PICC Interface"));
        assertTrue(ReaderWrapper.supportsPassThrough("ACS ACR1222 3S PICC Reader"));
        // these answer 63 00 or are not PN53x based
        assertFalse(ReaderWrapper.supportsPassThrough("ACS ACR1252 1S CL Reader PICC"));
        assertFalse(ReaderWrapper.supportsPassThrough("ACS ACR1255U-J1 PICC"));
        assertFalse(ReaderWrapper.supportsPassThrough("ACS ACR1283U"));
        assertFalse(ReaderWrapper.supportsPassThrough(null));
    }
}
