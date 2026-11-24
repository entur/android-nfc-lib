package no.entur.android.nfc.external.hid.card;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210DesfireWrapperTest {

    private static final String CHALLENGE = "00112233445566778899AABBCCDDEEFF";

    @Test
    public void testAuthenticateIsNotChained() throws Exception {
        // legacy, ISO, AES, EV2 first and EV2 non-first
        for (String command : new String[] { "0A00", "1A00", "AA00", "7100", "7700" }) {
            Atr210CardCommands commands = mock(Atr210CardCommands.class);
            // the card answers with the challenge and additional frame, the next frame is computed by the caller
            when(commands.transceive(any(byte[].class))).thenReturn(hex(CHALLENGE + "91AF"));

            Atr210DesfireWrapper wrapper = new Atr210DesfireWrapper(commands, false);

            assertArrayEquals(command, hex("AF" + CHALLENGE), wrapper.transceive(hex(command)));
            verify(commands, times(1)).transceive(any(byte[].class));
        }
    }

    @Test
    public void testChain() throws Exception {
        Atr210CardCommands commands = mock(Atr210CardCommands.class);
        when(commands.transceive(any(byte[].class))).thenReturn(hex("AABB91AF"), hex("CC9100"));

        Atr210DesfireWrapper wrapper = new Atr210DesfireWrapper(commands, false);

        // GET_VERSION
        assertArrayEquals(hex("00AABBCC"), wrapper.transceive(hex("60")));

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(commands, times(2)).transceive(captor.capture());
        List<byte[]> requests = captor.getAllValues();
        assertArrayEquals(hex("9060000000"), requests.get(0));
        assertArrayEquals(hex("90AF000000"), requests.get(1));
    }

    @Test
    public void testIsoErrorIsNotDesfireOk() throws Exception {
        Atr210CardCommands commands = mock(Atr210CardCommands.class);
        // wrong length
        when(commands.transceive(any(byte[].class))).thenReturn(hex("6700"));

        Atr210DesfireWrapper wrapper = new Atr210DesfireWrapper(commands, false);

        // not 00, which would be DESFire OK
        assertArrayEquals(hex("6700"), wrapper.transceive(hex("60")));
        assertArrayEquals(hex("6700"), wrapper.transceive(hex("0A00")));
        assertArrayEquals(hex("6700"), Atr210DesfireWrapper.responseADPUToRaw(hex("6700")));
    }

    @Test
    public void testIsoApdusArePassedThrough() throws Exception {
        for (String apdu : new String[] { "00A4040007A000000003101000", "80CA9F7F00", "84CA9F7F00", "94A4000002200000", "905A00000300805700" }) {
            Atr210CardCommands commands = mock(Atr210CardCommands.class);
            when(commands.transceive(any(byte[].class))).thenReturn(hex("9000"));

            Atr210DesfireWrapper wrapper = new Atr210DesfireWrapper(commands, false);

            assertArrayEquals(apdu, hex("9000"), wrapper.transceive(hex(apdu)));
            verify(commands).transceive(hex(apdu));
        }
    }

    private static byte[] hex(String string) {
        return ByteArrayHexStringConverter.hexStringToByteArray(string);
    }
}
