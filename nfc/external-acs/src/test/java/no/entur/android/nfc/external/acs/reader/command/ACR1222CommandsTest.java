package no.entur.android.nfc.external.acs.reader.command;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.smartcard.Reader;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.AcrPICC;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1222LReaderControl;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1222LCommandWrapper;

public class ACR1222CommandsTest {

    private static final String NAME = "ACS ACR1222L 3S CL Reader";

    /**
     * Reader API -> remote command wrapper -> commands -> reader, with the reader responding as an ACR1222L does.
     */

    private static Acr1222LReader reader(ReaderWrapper readerWrapper) throws Exception {
        IAcr1222LCommandWrapper wrapper = new IAcr1222LCommandWrapper(new ACR1222Commands(NAME, readerWrapper));

        IAcr1222LReaderControl control = mock(IAcr1222LReaderControl.class);
        when(control.setPICC(anyInt())).thenAnswer((i) -> wrapper.setPICC(i.getArgument(0)));
        when(control.getPICC()).thenAnswer((i) -> wrapper.getPICC());

        return new Acr1222LReader(NAME, "acs-1", control);
    }

    @Test
    public void testSetPICCWithAutomaticPolling() throws Exception {
        ReaderWrapper readerWrapper = mock(ReaderWrapper.class);
        // ACR1222L: PICC operating parameter followed by 90 00
        when(readerWrapper.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xE3, (byte) 0x90, 0x00 });

        Acr1222LReader reader = reader(readerWrapper);

        assertTrue(reader.setPICC(AcrPICC.AUTO_PICC_POLLING, AcrPICC.AUTO_ATS_GENERATION, AcrPICC.POLLING_INTERVAL, AcrPICC.POLL_ISO14443_TYPE_A,
                AcrPICC.POLL_ISO14443_TYPE_B));

        ArgumentCaptor<byte[]> command = ArgumentCaptor.forClass(byte[].class);
        verify(readerWrapper).control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), command.capture());
        assertArrayEquals(new byte[] { (byte) 0xFF, 0x00, 0x51, (byte) 0xE3, 0x00 }, command.getValue());
    }

    @Test
    public void testGetPICC() throws Exception {
        ReaderWrapper readerWrapper = mock(ReaderWrapper.class);
        when(readerWrapper.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xE3, (byte) 0x90, 0x00 });

        List<AcrPICC> picc = reader(readerWrapper).getPICC();

        assertEquals(Arrays.asList(AcrPICC.AUTO_PICC_POLLING, AcrPICC.AUTO_ATS_GENERATION, AcrPICC.POLLING_INTERVAL, AcrPICC.POLL_ISO14443_TYPE_B,
                AcrPICC.POLL_ISO14443_TYPE_A), picc);
    }

    @Test
    public void testSetACR122PICC() throws Exception {
        ReaderWrapper readerWrapper = mock(ReaderWrapper.class);
        when(readerWrapper.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xFF, (byte) 0x90, 0x00 });

        ACR1222Commands commands = new ACR1222Commands(NAME, readerWrapper);

        assertTrue(commands.setACR122PICC(0, 0xFF));
    }

    @Test
    public void testGetACR122PICC() throws Exception {
        ReaderWrapper readerWrapper = mock(ReaderWrapper.class);
        when(readerWrapper.control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), any(byte[].class))).thenReturn(new byte[] { (byte) 0xE3, (byte) 0x90, 0x00 });

        ACR1222Commands commands = new ACR1222Commands(NAME, readerWrapper);

        assertEquals(0xE3, commands.getACR122PICC(0).intValue());
    }
}
