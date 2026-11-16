package no.entur.android.nfc.external.acs.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.acs.smartcard.ReaderException;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.entur.android.nfc.external.acs.reader.bind.IAcr1252UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1255UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1281UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1552UBinder;
import no.entur.android.nfc.external.acs.reader.command.ACR1252Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1255Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1281Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1552Commands;

/**
 * Verify that readers decode the responses encoded by the corresponding command wrappers.
 * Only the reader commands are mocked; reader, binder and command wrapper are real.
 */

@RunWith(AndroidJUnit4.class)
public class AcrReaderRemoteCommandTest {

    @Test
    public void testAcr1255UGetLEDs() throws Exception {
        ACR1255Commands commands = mock(ACR1255Commands.class);
        when(commands.getLED2(0)).thenReturn(Acr1255UReader.LED_1_GREEN | Acr1255UReader.LED_2_BLUE);

        List<Set<AcrLED>> leds = acr1255U(commands).getLEDs();

        assertEquals(Collections.singleton(AcrLED.GREEN), leds.get(0));
        assertEquals(Collections.singleton(AcrLED.BLUE), leds.get(1));
    }

    @Test
    public void testAcr1255UGetLEDsFailure() throws Exception {
        ACR1255Commands commands = mock(ACR1255Commands.class);
        when(commands.getLED2(0)).thenThrow(new ReaderException("test"));

        Acr1255UReader reader = acr1255U(commands);

        assertThrows(AcrReaderException.class, reader::getLEDs);
    }

    @Test
    public void testAcr1255UGetSerialNumber() throws Exception {
        ACR1255Commands commands = mock(ACR1255Commands.class);
        when(commands.getSerialNumber(0)).thenReturn("RR123-456789");

        assertEquals("RR123-456789", acr1255U(commands).getSerialNumber());
    }

    @Test
    public void testAcr1255USetAutomaticPICCPollingSpeed() throws Exception {
        ACR1255Commands commands = mock(ACR1255Commands.class);
        when(commands.setAutoPPS(0, (byte) 1, (byte) 2)).thenReturn(new byte[]{1, 0, 2, 0});

        assertTrue(acr1255U(commands).setAutomaticPICCPollingSpeed(AcrCommunicationSpeed.RATE_212_KBPS, AcrCommunicationSpeed.RATE_424_KBPS));

        verify(commands).setAutoPPS(0, (byte) 1, (byte) 2);
    }

    @Test
    public void testAcr1252UGetLEDs() throws Exception {
        ACR1252Commands commands = mock(ACR1252Commands.class);
        when(commands.getLED2(0)).thenReturn(0b11); // red | green

        IAcr1252UBinder binder = new IAcr1252UBinder();
        binder.setCommands(commands);

        assertLEDs(new Acr1252UReader("ACR1252U", "acs-1", binder).getLEDs(), AcrLED.RED, AcrLED.GREEN);
    }

    @Test
    public void testAcr1281UGetLEDs() throws Exception {
        ACR1281Commands commands = mock(ACR1281Commands.class);
        when(commands.getLED2(0)).thenReturn(0b10); // green

        IAcr1281UBinder binder = new IAcr1281UBinder();
        binder.setCommands(commands);

        assertLEDs(new Acr1281UReader("ACR1281U", "acs-1", binder).getLEDs(), AcrLED.GREEN);
    }

    @Test
    public void testAcr1552UGetLEDs() throws Exception {
        ACR1552Commands commands = mock(ACR1552Commands.class);
        when(commands.getLED2(0)).thenReturn(Acr1552UReader.LED_BLUE | Acr1552UReader.LED_GREEN);

        IAcr1552UBinder binder = new IAcr1552UBinder();
        binder.setCommands(commands);

        assertLEDs(new Acr1552UReader("ACR1552U", "acs-1", binder).getLEDs(), AcrLED.BLUE, AcrLED.GREEN);
    }

    private Acr1255UReader acr1255U(ACR1255Commands commands) {
        IAcr1255UBinder binder = new IAcr1255UBinder();
        binder.setCommands(commands);

        return new Acr1255UReader("ACR1255U", "acs-1", binder);
    }

    private void assertLEDs(List<AcrLED> actual, AcrLED... expected) {
        assertEquals(new HashSet<>(Arrays.asList(expected)), new HashSet<>(actual));
    }
}
