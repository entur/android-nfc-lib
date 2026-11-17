package no.entur.android.nfc.websocket.server.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javax.smartcardio.CardException;

import no.entur.android.nfc.websocket.server.FakeCard;
import no.entur.android.nfc.websocket.server.FakeCardTerminal;

public class Acr1252CardTerminalTest {

    @Test
    public void testSetAutomaticPICCPolling() throws CardException {
        // E0 00 00 23 01 <value> -> E1 00 00 00 01 <value>
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1252 Dual Reader PICC", () -> new FakeCard((command) -> new byte[]{(byte) 0xE1, 0x00, 0x00, 0x00, 0x01, command[5]}));

        Acr1252CardTerminal acr1252CardTerminal = new Acr1252CardTerminal(terminal);

        assertTrue(acr1252CardTerminal.setAutomaticPICCPolling(0b11000001));
        assertTrue(acr1252CardTerminal.setAutomaticPICCPolling(0));

        assertDisconnected(terminal);
    }

    @Test
    public void testSetAutomaticPICCPollingUnexpectedValue() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1252 Dual Reader PICC", () -> new FakeCard((command) -> new byte[]{(byte) 0xE1, 0x00, 0x00, 0x00, 0x01, 0x00}));

        Acr1252CardTerminal acr1252CardTerminal = new Acr1252CardTerminal(terminal);

        assertFalse(acr1252CardTerminal.setAutomaticPICCPolling(0b11000001));

        assertDisconnected(terminal);
    }

    @Test
    public void testSetPICCAndFirmware() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1252 Dual Reader PICC", () -> new FakeCard((command) -> {
            if(command[2] == 0x51) {
                // set PICC
                return new byte[]{(byte) 0x90, command[3]};
            }
            return "ACR1252U".getBytes();
        }));

        Acr1252CardTerminal acr1252CardTerminal = new Acr1252CardTerminal(terminal);

        assertTrue(acr1252CardTerminal.setPICC(0x03));
        assertEquals("ACR1252U", acr1252CardTerminal.getFirmware());

        assertDisconnected(terminal);
    }

    @Test
    public void testDisconnectOnError() {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1252 Dual Reader PICC", () -> new FakeCard((command) -> new byte[]{0x63, 0x00}));

        Acr1252CardTerminal acr1252CardTerminal = new Acr1252CardTerminal(terminal);

        try {
            acr1252CardTerminal.setPICC(0x03);
        } catch(CardException e) {
            // expected
        }

        assertDisconnected(terminal);
    }

    private static void assertDisconnected(FakeCardTerminal terminal) {
        assertFalse(terminal.getConnected().isEmpty());
        for (FakeCard card : terminal.getConnected()) {
            assertTrue(card.isDisconnected());
        }
    }
}
