package no.entur.android.nfc.external.acs.reader.command;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.smartcard.Reader;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1222LCommandWrapper;

public class ACR1222CommandsTest {

    @Test
    public void testSetPICCKeepsAutomaticPolling() throws Exception {
        ReaderWrapper reader = mock(ReaderWrapper.class);
        // ACR1222L: PICC operating parameter followed by 90 00
        when(reader.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xE3, (byte) 0x90, 0x00 });

        IAcr1222LCommandWrapper wrapper = new IAcr1222LCommandWrapper(new ACR1222Commands("ACS ACR1222L 3S CL Reader", reader));

        // type A and B
        wrapper.setPICC(0x03);

        ArgumentCaptor<byte[]> command = ArgumentCaptor.forClass(byte[].class);
        verify(reader).control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), command.capture());
        // auto PICC polling, auto ATS generation, 250 ms polling interval, type A and B
        assertArrayEquals(new byte[] { (byte) 0xFF, 0x00, 0x51, (byte) 0xE3, 0x00 }, command.getValue());
    }

    @Test
    public void testSetACR122PICC() throws Exception {
        ReaderWrapper reader = mock(ReaderWrapper.class);
        when(reader.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xFF, (byte) 0x90, 0x00 });

        ACR1222Commands commands = new ACR1222Commands("ACS ACR1222L 3S CL Reader", reader);

        assertTrue(commands.setACR122PICC(0, 0xFF));
    }

    @Test
    public void testGetACR122PICC() throws Exception {
        ReaderWrapper reader = mock(ReaderWrapper.class);
        when(reader.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xE3, (byte) 0x90, 0x00 });

        ACR1222Commands commands = new ACR1222Commands("ACS ACR1222L 3S CL Reader", reader);

        assertEquals(0xE3, commands.getACR122PICC(0).intValue());
    }
}
