package no.entur.android.nfc.external.acs.reader.command;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.acs.smartcard.Reader;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1281UReaderControl;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1552UReaderControl;
import no.entur.android.nfc.external.acs.reader.command.remote.FontSet;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1281UCommandWrapper;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1552UCommandWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class AcsReaderCommandsTest {

    /**
     * @param response the reader's response to the escape command
     * @return reader which responds with the given bytes
     */

    private static ReaderWrapper reader(String response) throws Exception {
        // the ACS reader is not used, so skip the constructor
        ReaderWrapper reader = mock(ReaderWrapper.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        doReturn(hex(response)).when(reader).control(anyInt(), anyInt(), any(byte[].class));
        return reader;
    }

    private static byte[] command(ReaderWrapper reader) throws Exception {
        ArgumentCaptor<byte[]> command = ArgumentCaptor.forClass(byte[].class);
        verify(reader).control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), command.capture());
        return command.getValue();
    }

    @Test
    public void testAcr1255SettersReturnTrueOnSuccess() throws Exception {
        assertTrue(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000101")).setAntennaField(0, true));
        assertFalse(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000100")).setAntennaField(0, true));

        assertTrue(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000102")).setBluetoothTransmissionPower(0, (byte) 2));
        assertFalse(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000103")).setBluetoothTransmissionPower(0, (byte) 2));

        assertTrue(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000101")).setSleepModeOption(0, (byte) 1));
        assertFalse(new ACR1255UsbCommands("ACS ACR1255U-J1", reader("E10000000102")).setSleepModeOption(0, (byte) 1));
    }

    @Test
    public void testAcr1222SetLED() throws Exception {
        // 4 LEDs control: FF 00 44 bLEDsState 00, response SW1 SW2
        ReaderWrapper reader = reader("9000");
        assertTrue(new ACR1222Commands("ACS ACR1222L", reader).setLED(0, 0x05));
        assertArrayEquals(hex("FF00440500"), command(reader));

        assertFalse(new ACR1222Commands("ACS ACR1222L", reader("6300")).setLED(0, 0x05));
    }

    @Test
    public void testAcr1283SetLED() throws Exception {
        ReaderWrapper reader = reader("9000");
        assertTrue(new ACR1283Commands("ACS ACR1283L", reader).setLED(0, 0x0F));
        assertArrayEquals(hex("FF00440F00"), command(reader));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcr1283SetLEDFails() throws Exception {
        new ACR1283Commands("ACS ACR1283L", reader("6300")).setLED(0, 0x0F);
    }

    @Test
    public void testAcr1222BehaviourBits() {
        // card insertion / removal buzzer is bit 4 on the ACR1222L (checked on a reader), not bit 3 as on the ACR1283L
        assertEquals(0x10, Acr1222LReader.serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER));
        assertEquals(0x96, Acr1222LReader.serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED,
                AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER,
                AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED));
        // factory default 0x8F
        assertEquals(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED,
                AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), Acr1222LReader.parseBehaviour(0x8F));
    }

    @Test
    public void testAcr1222Buzzer() throws Exception {
        // buzzer control E0 00 00 28 01 <duration in 10 ms>, response E1 00 00 00 01 <timer> (checked on a reader)
        ReaderWrapper reader = mock(ReaderWrapper.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.doAnswer(invocation -> {
            byte[] response = hex("E10000000100");
            System.arraycopy(response, 0, invocation.getArgument(4), 0, response.length);
            return response.length;
        }).when(reader).control(anyInt(), anyInt(), any(byte[].class), anyInt(), any(byte[].class), anyInt());

        assertTrue(new ACR1222Commands("ACS ACR1222L", reader).setBuzzerBeepDurationOnCardDetection(0, 10));

        ArgumentCaptor<byte[]> command = ArgumentCaptor.forClass(byte[].class);
        verify(reader).control(eq(0), eq(Reader.IOCTL_CCID_ESCAPE), command.capture(), anyInt(), any(byte[].class), anyInt());
        assertArrayEquals(hex("E0000028010A"), command.getValue());
    }

    @Test
    public void testAcr1552SetLEDs() throws Exception {
        // writing: bit 0 blue, bit 1 green, per the reference manual (checked on a reader)
        ReaderWrapper readerWrapper = reader("E10000000101");
        IAcr1552UCommandWrapper wrapper = new IAcr1552UCommandWrapper(new ACR1552Commands("ACS ACR1552 1S CL Reader", readerWrapper));
        IAcr1552UReaderControl control = mock(IAcr1552UReaderControl.class);
        when(control.setLEDs(anyInt())).thenAnswer((i) -> wrapper.setLEDs(i.getArgument(0)));

        Acr1552UReader reader = new Acr1552UReader("ACS ACR1552 1S CL Reader", "acs-1", control);

        assertTrue(reader.setLEDs(AcrLED.BLUE));
        assertArrayEquals(hex("E00000290101"), command(readerWrapper));
    }

    @Test
    public void testAcr1552GetLEDs() throws Exception {
        // reading: bit 0 green, bit 1 blue (seen on a reader with firmware 1.01.01; the manual has them the other way around)
        IAcr1552UCommandWrapper wrapper = new IAcr1552UCommandWrapper(new ACR1552Commands("ACS ACR1552 1S CL Reader", reader("E10000000102")));
        IAcr1552UReaderControl control = mock(IAcr1552UReaderControl.class);
        when(control.getLEDs()).thenAnswer((i) -> wrapper.getLEDs());

        Acr1552UReader reader = new Acr1552UReader("ACS ACR1552 1S CL Reader", "acs-1", control);

        assertEquals(Collections.singletonList(AcrLED.BLUE), reader.getLEDs());
    }

    @Test
    public void testFontNordicCharacters() {
        // font A: ISO 8859-1
        assertArrayEquals(hex("C6D8C5E6F8E5"), AcrFont.FONT_A.mapString("ÆØÅæøå"));
        // font C: code page 437
        assertArrayEquals(hex("92918F86"), AcrFont.FONT_C.mapString("ÆæÅå"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFontCHasNoSlashedO() {
        AcrFont.FONT_C.mapString("Ø");
    }

    @Test
    public void testAcr1255AutomaticPolling() throws Exception {
        // the response echoes the command in P2
        ReaderWrapper reader = reader("E100004001");
        assertTrue(new ACR1255UsbCommands("ACS ACR1255U-J1", reader).setAutomaticPolling(0, true));
        assertArrayEquals(hex("E000004001"), command(reader));
    }

    @Test
    public void testAcr1552SetPICC() throws Exception {
        assertTrue(new ACR1552Commands("ACS ACR1552 1S CL Reader", reader("E100000002" + "0103")).setPICC(0, 0x0103));
        assertFalse(new ACR1552Commands("ACS ACR1552 1S CL Reader", reader("E100000002" + "0101")).setPICC(0, 0x0103));
    }

    @Test
    public void testAcr122BuzzerForCardDetection() throws Exception {
        ReaderWrapper reader = reader("9000");
        assertTrue(new ACR122Commands("ACS ACR122U PICC Interface", reader).setBuzzerForCardDetection(0, false));
        assertArrayEquals(hex("FF00520000"), command(reader));

        assertFalse(new ACR122Commands("ACS ACR122U PICC Interface", reader("6300")).setBuzzerForCardDetection(0, false));
    }

    @Test
    public void testAcr122TimeoutParameter() throws Exception {
        assertTrue(new ACR122Commands("ACS ACR122U PICC Interface", reader("9000")).setTimeoutParameter(0, 1));
    }

    @Test
    public void testAcr1222Commands() throws Exception {
        String name = "ACS ACR1222L 3S CL Reader";
        assertTrue(new ACR1222Commands(name, reader("9000")).beepBuzzer(0, 1, 1, 1));
        assertTrue(new ACR1222Commands(name, reader("9000")).setTimeoutParameter(0, 1));
        assertTrue(new ACR1222Commands(name, reader("9000")).blinkLEDAndBeepBuzzer(0, 0, 1, 1, 1, 0));
        assertTrue(new ACR1222Commands(name, reader("9000")).setLEDOnCardDetection(0, true));
        assertTrue(new ACR1222Commands(name, reader("9000")).lightLED(0, true, false, false, false));

        assertFalse(new ACR1222Commands(name, reader("6300")).beepBuzzer(0, 1, 1, 1));
        assertFalse(new ACR1222Commands(name, reader("63")).lightLED(0, true, false, false, false));
    }

    @Test
    public void testAcr1252SetBuzzer() throws Exception {
        ReaderWrapper reader = reader("E10000000100");
        assertTrue(new ACR1252Commands("ACS ACR1252 1S CL Reader", reader).setBuzzer(0, true));
        assertArrayEquals(hex("E0000028" + "01FF"), command(reader));

        assertFalse(new ACR1252Commands("ACS ACR1252 1S CL Reader", reader("E10000000101")).setBuzzer(0, false));
    }

    @Test
    public void testAcr1283Behaviour() {
        // bit 1 polling LED, bit 2 activation LED
        assertEquals(Collections.singletonList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED), Acr1283LReader.parseBehaviour(0x02));
        assertEquals(Collections.singletonList(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED), Acr1283LReader.parseBehaviour(0x04));
        assertEquals(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED,
                AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED),
                Acr1283LReader.parseBehaviour(0x8E));

        // the buzzer is bit 3 per the manual, bit 4 on an ACR1283U: set both, read either
        assertEquals(0x18, Acr1283LReader.serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER));
        assertEquals(Collections.singletonList(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER), Acr1283LReader.parseBehaviour(0x08));
        assertEquals(Collections.singletonList(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER), Acr1283LReader.parseBehaviour(0x10));
    }

    @Test
    public void testFont3() throws Exception {
        // the font set is bits 4 and 5 of the option byte
        ReaderWrapper reader = reader("9000");
        new ACR1222Commands("ACS ACR1222L 3S CL Reader", reader).displayText(0, FontSet.FONT_3, false, 0, 0, "A".getBytes());
        assertEquals(0x20, command(reader)[1] & 0xFF);
    }

    @Test
    public void testAcr1552AutomaticCommunicationSpeed() throws Exception {
        // configured max 424 kbps, current 106 kbps
        IAcr1552UCommandWrapper wrapper = new IAcr1552UCommandWrapper(new ACR1552Commands("ACS ACR1552 1S CL Reader", reader("E100000002" + "0200")));
        IAcr1552UReaderControl control = mock(IAcr1552UReaderControl.class);
        when(control.setAutomaticCommunicationSpeed(anyInt())).thenAnswer((i) -> wrapper.setAutomaticCommunicationSpeed(i.getArgument(0)));

        Acr1552UReader reader = new Acr1552UReader("ACS ACR1552 1S CL Reader", "acs-1", control);

        assertTrue(reader.setAutomaticCommunicationSpeed(AcrCommunicationSpeed.RATE_424_KBPS));
    }

    @Test
    public void testAcr1281SetExclusiveMode() throws Exception {
        // configured exclusive, current shared
        IAcr1281UCommandWrapper wrapper = new IAcr1281UCommandWrapper(new ACR1281Commands("ACS ACR1281 1S Dual Reader", reader("E100000002" + "0100")));
        IAcr1281UReaderControl control = mock(IAcr1281UReaderControl.class);
        when(control.setExclusiveMode(anyBoolean())).thenAnswer((i) -> wrapper.setExclusiveMode(i.getArgument(0)));

        Acr1281UReader reader = new Acr1281UReader("ACS ACR1281 1S Dual Reader", "acs-1", control);

        // exclusive, i.e. not shared
        assertTrue(reader.setExclusiveMode(false));
        assertFalse(reader.setExclusiveMode(true));
    }

    private static byte[] hex(String string) {
        return ByteArrayHexStringConverter.hexStringToByteArray(string);
    }
}
